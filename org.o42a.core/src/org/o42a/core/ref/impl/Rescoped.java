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
package org.o42a.core.ref.impl;

import static org.o42a.core.artifact.link.TargetRef.targetRef;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStructFinder;


public final class Rescoped extends Ref {

	private final Ref ref;
	private final Rescoper rescoper;

	public Rescoped(Ref ref, Rescoper rescoper, Distributor distributor) {
		super(
				ref,
				distributor,
				ref.getLogical().rescope(rescoper));
		this.ref = ref;
		this.rescoper = rescoper;
	}

	@Override
	public boolean isConstant() {
		if (!getRescoper().isStatic()) {
			return false;
		}
		return getResolution().isConstant();
	}

	public final Rescoper getRescoper() {
		return this.rescoper;
	}

	@Override
	public Resolution resolve(Resolver resolver) {

		final Path path = getPath();

		if (path != null) {
			return resolver.path(
					pathResolver(resolver),
					path.bind(this, getScope()),
					getScope());
		}

		final Resolver rescoped = this.rescoper.rescope(resolver);

		if (rescoped == null) {
			return null;
		}

		return this.ref.resolve(rescoped);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return this.ref.value(this.rescoper.rescope(resolver));
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Scope rescoped =
				this.rescoper.rescope(reproducer.getReproducingScope());
		final Reproducer rescopedReproducer =
				reproducer.reproducerOf(rescoped);

		if (rescopedReproducer == null) {
			getLogger().notReproducible(this);
			return null;
		}

		final Rescoper rescoper = this.rescoper.reproduce(reproducer);

		if (rescoper == null) {
			return null;
		}

		final Ref ref = this.ref.reproduce(rescopedReproducer);

		if (ref == null) {
			return null;
		}

		return new RescopedRef(ref, rescoper);
	}

	@Override
	public Ref rescope(Rescoper rescoper) {

		final Rescoper newRescoper = this.rescoper.and(rescoper);

		if (this.rescoper.equals(newRescoper)) {
			return this;
		}

		return new RescopedRef(this.ref, newRescoper);
	}

	@Override
	public TypeRef toTypeRef(ValueStructFinder valueStructFinder) {

		final TypeRef typeRef = this.ref.toTypeRef(valueStructFinder);

		return typeRef.rescope(this.rescoper);
	}

	@Override
	public StaticTypeRef toStaticTypeRef(ValueStructFinder valueStructFinder) {

		final StaticTypeRef typeRef =
				this.ref.toStaticTypeRef(valueStructFinder);

		return typeRef.rescope(this.rescoper);
	}

	@Override
	public Rescoper toRescoper() {
		return this.ref.toRescoper().and(this.rescoper);
	}

	@Override
	public TargetRef toTargetRef(TypeRef typeRef) {
		return targetRef(this.ref, typeRef, this.rescoper);
	}

	@Override
	public String toString() {
		return "Rescoped[" + this.rescoper + "](" + this.ref + ')';
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return new RescopedDefinition(this, this.ref.toFieldDefinition());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.ref.resolveAll(this.rescoper.rescope(resolver));
		this.rescoper.resolveAll(resolver);
		resolve(resolver).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver).resolveValues(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Op extends RefOp {

		private Op(HostOp host, Rescoped ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			rescope(dirs).writeLogicalValue(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return rescope(dirs.dirs()).writeValue(dirs);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return rescope(dirs).target(dirs);
		}


		private RefOp rescope(CodeDirs dirs) {

			final Rescoped rescoped = (Rescoped) getRef();
			final HostOp host = rescoped.rescoper.rescope(dirs, host());

			return rescoped.ref.op(host);
		}

	}

}
