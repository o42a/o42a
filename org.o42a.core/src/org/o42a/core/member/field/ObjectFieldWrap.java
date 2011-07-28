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

import org.o42a.core.artifact.common.ObjectWrap;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;


final class ObjectFieldWrap extends FieldWrap<Obj> {

	ObjectFieldWrap(MemberOwner owner, Field<Obj> type, Field<Obj> wrapped) {
		super(owner, type, wrapped);
	}

	private ObjectFieldWrap(MemberOwner owner, FieldWrap<Obj> overridden) {
		super(owner, overridden);
	}

	@Override
	protected Obj wrapArtifact() {
		return new Wrap(this);
	}

	@Override
	protected Field<Obj> propagate(MemberOwner owner) {
		return new ObjectFieldWrap(owner, this);
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
		protected Obj createWrapped() {
			return field().getWrapped().getArtifact();
		}

		@Override
		protected Ascendants buildAscendants() {

			final Ascendants ascendants = new Ascendants(this);

			return ascendants.setAncestor(new AncestorRef(this).toTypeRef());
		}

		private ObjectFieldWrap field() {
			return (ObjectFieldWrap) getScope();
		}

	}

	private static final class AncestorRef extends Ref {

		private final Wrap wrap;

		AncestorRef(Wrap wrap) {
			super(
					wrap,
					wrap.distributeIn(wrap.getScope().getEnclosingContainer()));
			this.wrap = wrap;
		}

		@Override
		public Resolution resolve(Resolver resolver) {
			assertScopeIs(resolver.getScope());
			return resolver.artifactPart(
					this,
					this.wrap,
					field().getInterface().getArtifact());
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		protected FieldDefinition createFieldDefinition() {
			return defaultFieldDefinition();
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
			resolve(resolver).resolveAll();
		}

		@Override
		protected void fullyResolveValues(Resolver resolver) {
			resolve(resolver).resolveValues(resolver);
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
