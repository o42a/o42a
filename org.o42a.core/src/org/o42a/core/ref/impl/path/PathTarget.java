/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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


public final class PathTarget extends Ref {

	private final BoundPath path;

	public PathTarget(
			LocationInfo location,
			Distributor distributor,
			BoundPath path) {
		super(location, distributor);
		this.path = path;
	}

	@Override
	public boolean isKnownStatic() {
		return this.path.getRawPath().isStatic();
	}

	@Override
	public boolean isStatic() {
		return this.path.isStatic();
	}

	@Override
	public boolean isConstant() {
		return isStatic() && getResolution().isConstant();
	}

	@Override
	public Path getPath() {
		return this.path.getRawPath();
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

		return new PathTarget(this, distribute(), materialized);
	}

	@Override
	public Ref rescope(Rescoper rescoper) {
		if (!(rescoper instanceof PathRescoper)) {
			return super.rescope(rescoper);
		}

		final PathRescoper pathRescoper = (PathRescoper) rescoper;
		final Path rescopePath = pathRescoper.getPath().getRawPath();

		return rescopePath.append(this.path.getRawPath()).target(
				this,
				distributeIn(rescoper.getFinalScope().getContainer()));
	}

	@Override
	public Ref upgradeScope(Scope scope) {
		if (getScope() == scope) {
			return this;
		}
		return new PathTarget(
				this,
				distributeIn(scope.getContainer()),
				this.path);
	}

	@Override
	public Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return new PathTarget(this, distribute(), this.path.toStatic());
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final BoundPath path = this.path;

		if (this.path.isAbsolute()) {
			return this.path.getPath().target(this, reproducer.distribute());
		}

		final PathReproduction pathReproduction = path.reproduce(reproducer);

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return reproducePart(
						reproducer,
						pathReproduction.getExternalPath());
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startWithPrefix(
					reproducer,
					pathReproduction,
					reproducer.getPhrasePrefix().getPath().materialize());
		}

		final PathTarget reproducedPart = reproducePart(
				reproducer,
				pathReproduction.getReproducedPath());

		if (!pathReproduction.isOutOfClause()) {
			return reproducedPart;
		}

		return startWithPrefix(
				reproducer,
				pathReproduction,
				reproducedPart.getPath().append(
						reproducer.getPhrasePrefix().getPath().materialize()));
	}

	@Override
	public Rescoper toRescoper() {
		return this.path.rescoper();
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
		return this.path.getRawPath().fieldDefinition(this, distribute());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver, fullPathResolver(resolver)).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {

		final Resolution resolution =
				resolve(resolver, valuePathResolver(resolver));

		resolution.resolveValues(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private Resolution resolve(Resolver resolver, PathResolver pathResolver) {
		return resolver.path(pathResolver, this.path, resolver.getScope());
	}

	private PathTarget reproducePart(Reproducer reproducer, Path path) {
		return new PathTarget(
				this,
				reproducer.distribute(),
				path.bind(this, reproducer.getScope()));
	}

	private Ref startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction,
			Path phrasePrefix) {

		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix.target(this, reproducer.distribute());
		}

		return phrasePrefix.append(externalPath).target(
				this,
				reproducer.distribute());
	}

	private static final class Op extends RefOp {

		Op(HostOp host, PathTarget ref) {
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

			final PathTarget ref = (PathTarget) getRef();

			return ref.path.op(dirs, host());
		}

	}

}
