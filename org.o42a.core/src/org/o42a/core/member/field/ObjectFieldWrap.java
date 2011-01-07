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
package org.o42a.core.member.field;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.ObjectWrap;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ex;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;


final class ObjectFieldWrap extends FieldWrap<Obj> {

	ObjectFieldWrap(
			Container enclosingContainer,
			Field<?> type,
			Field<?> wrapped) {
		super(enclosingContainer, type, wrapped);
	}

	private ObjectFieldWrap(
			Container enclosingContainer,
			FieldWrap<Obj> overridden) {
		super(enclosingContainer, overridden);
	}

	@Override
	protected Obj wrapArtifact() {
		return new Wrap(this);
	}

	@Override
	protected Field<Obj> propagate(Scope enclosingScope) {
		return new ObjectFieldWrap(enclosingScope.getContainer(), this);
	}

	@Override
	protected Obj propagateArtifact(Field<Obj> overridden) {
		return new Wrap(this, overridden.getArtifact());
	}

	private static final class Wrap extends ObjectWrap {

		Wrap(ObjectFieldWrap scope, Obj sample) {
			super(scope, sample);
		}

		Wrap(ObjectFieldWrap scope) {
			super(scope);
		}

		@Override
		public Obj getWrapped() {
			return field().getWrapped().getArtifact();
		}

		@Override
		protected Ascendants buildAscendants() {

			final Ascendants ascendants = new Ascendants(getScope());

			return ascendants.setAncestor(new AncestorEx(this).toTypeRef());
		}

		private ObjectFieldWrap field() {
			return (ObjectFieldWrap) getScope();
		}

	}

	private static final class AncestorEx extends Ex {

		private final Wrap wrap;

		AncestorEx(Wrap wrap) {
			super(
					wrap,
					wrap.distributeIn(wrap.getScope().getEnclosingContainer()));
			this.wrap = wrap;
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		protected Resolution resolveExpression(Scope scope) {
			assertScopeIs(scope);
			return artifactResolution(field().getInterface().getArtifact());
		}

		@Override
		protected RefOp createOp(HostOp host) {
			throw new UnsupportedOperationException();
		}

		private ObjectFieldWrap field() {
			return this.wrap.field();
		}

	}

}
