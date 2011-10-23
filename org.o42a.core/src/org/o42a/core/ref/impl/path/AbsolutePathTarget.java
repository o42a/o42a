/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathResolver.valuePathResolver;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.impl.rescoper.UpgradeRescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


public final class AbsolutePathTarget extends Ref {

	private final BoundPath path;

	public AbsolutePathTarget(BoundPath path, Distributor distributor) {
		super(path, distributor);
		this.path = path;
	}

	@Override
	public boolean isConstant() {
		return getResolution().isConstant();
	}

	@Override
	public boolean isKnownStatic() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public final Path getPath() {
		return this.path.getPath();
	}

	@Override
	public Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return resolve(resolver, pathResolver(resolver));
	}

	@Override
	public ValueAdapter valueAdapter(ValueStruct<?, ?> expectedStruct) {

		final Step[] steps = this.path.getSteps();

		if (steps.length == 0) {
			return super.valueAdapter(expectedStruct);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.valueAdapter(this, expectedStruct);
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.path.ancestor(location, distribute());
	}

	@Override
	public Ref materialize() {

		final BoundPath materialized = this.path.materialize();

		if (materialized == this.path) {
			return this;
		}

		return materialized.target(distribute());
	}

	@Override
	public Ref rescope(Rescoper rescoper) {
		if (rescoper instanceof UpgradeRescoper) {
			return upgradeScope(((UpgradeRescoper) rescoper).getFinalScope());
		}
		if (!(rescoper instanceof PathRescoper)) {
			return super.rescope(rescoper);
		}

		final PathRescoper pathRescoper = (PathRescoper) rescoper;
		final BoundPath rescopePath = pathRescoper.getPath();

		return rescopePath.append(this.path.getRawPath()).target(
				distributeIn(rescoper.getFinalScope().getContainer()));
	}

	@Override
	public Ref upgradeScope(Scope scope) {
		if (getScope() == scope) {
			return this;
		}
		return this.path.target(distributeIn(scope.getContainer()));
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		return this;
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return this.path.fieldDefinition(distribute());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver, fullPathResolver(resolver)).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver, valuePathResolver(resolver)).resolveValues(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private Resolution resolve(Resolver resolver, PathResolver pathResolver) {
		return resolver.path(
				pathResolver,
				this.path,
				getContext().getRoot().getScope());
	}

	private static final class Op extends RefOp {

		Op(HostOp host, AbsolutePathTarget ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			target(dirs).writeLogicalValue(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return target(dirs.dirs()).writeValue(dirs);
		}

		@Override
		public PathOp target(CodeDirs dirs) {

			final AbsolutePathTarget ref = (AbsolutePathTarget) getRef();

			return ref.path.staticOp(dirs);
		}

	}

}
