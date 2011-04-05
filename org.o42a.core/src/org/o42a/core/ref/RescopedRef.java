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
package org.o42a.core.ref;

import org.o42a.core.*;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


final class RescopedRef extends Wrap {

	private final Rescoped rescoped;

	RescopedRef(Ref ref, Rescoper rescoper) {
		super(
				ref,
				new RescopedDistrubutor(
						rescoper.getFinalScope(),
						ref.getPlace()));
		this.rescoped = new Rescoped(ref, rescoper, distribute());
	}

	@Override
	public String toString() {
		if (this.rescoped == null) {
			return super.toString();
		}
		return this.rescoped.toString();
	}

	@Override
	protected Ref resolveWrapped() {

		final Path path = this.rescoped.getPath();

		if (path == null) {
			return this.rescoped;
		}

		return path.target(this, distribute());
	}

	private static final class Rescoped extends Ref {

		private final Ref ref;
		private final Rescoper rescoper;
		private Path path;
		private boolean pathBuilt;

		Rescoped(Ref ref, Rescoper rescoper, Distributor distributor) {
			super(
					ref,
					distributor,
					ref.getLogical().rescope(rescoper));
			this.ref = ref;
			this.rescoper = rescoper;
		}

		@Override
		public Path getPath() {
			if (this.pathBuilt) {
				return this.path;
			}

			final Path refPath = this.ref.getPath();

			if (refPath == null) {
				this.pathBuilt = true;
				return null;
			}

			final Path rescoperPath = this.rescoper.getPath();

			if (rescoperPath == null) {
				this.pathBuilt = true;
				return null;
			}

			this.path = rescoperPath.append(refPath).rebuild();
			this.pathBuilt = true;

			return this.path;
		}

		@Override
		public Resolution resolve(Scope scope) {
			return this.ref.resolve(this.rescoper.rescope(scope));
		}

		@Override
		public Value<?> value(Scope scope) {
			return this.ref.value(this.rescoper.rescope(scope));
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Scope rescoped = this.rescoper.rescope(getScope());
			final Reproducer rescopedReproducer =
				reproducer.reproducerOf(rescoped);

			if (rescopedReproducer == null) {
				getLogger().notReproducible(this);
				return null;
			}

			final Rescoper rescoper = this.rescoper.reproduce(this, reproducer);

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
		public Rescoper toRescoper() {
			return this.ref.toRescoper().and(this.rescoper);
		}

		@Override
		public TypeRef toTypeRef() {
			return this.ref.toTypeRef().rescope(this.rescoper);
		}

		@Override
		public StaticTypeRef toStaticTypeRef() {
			return this.ref.toStaticTypeRef().rescope(this.rescoper);
		}

		@Override
		public TargetRef toTargetRef(TypeRef typeRef) {
			return createTargetRef(
					this.ref,
					typeRef,
					this.rescoper);
		}

		@Override
		public String toString() {
			return "Rescoped[" + this.rescoper + ": " + this.ref + ']';
		}

		@Override
		protected RefOp createOp(HostOp host) {
			return new Op(host, this);
		}

	}

	private static final class RescopedDistrubutor extends Distributor {

		private final Scope scope;
		private final ScopePlace place;

		RescopedDistrubutor(Scope scope, ScopePlace place) {
			this.scope = scope;
			this.place = place;
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

		@Override
		public Container getContainer() {
			return getScope().getContainer();
		}

		@Override
		public Scope getScope() {
			return this.scope;
		}

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
		public void writeValue(CodeDirs dirs, ValOp result) {
			rescope(dirs).writeValue(dirs, result);
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
