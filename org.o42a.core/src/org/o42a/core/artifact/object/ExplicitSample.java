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
package org.o42a.core.artifact.object;

import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ex;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class ExplicitSample extends Sample {

	private final StaticTypeRef explicitAscendant;
	private TypeRef ancestor;

	ExplicitSample(Scope scope, StaticTypeRef explicitAscendant) {
		super(explicitAscendant, scope);
		this.explicitAscendant = explicitAscendant;
		assertSameScope(explicitAscendant);
	}

	@Override
	public TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}

		final Obj type = this.explicitAscendant.getType();
		final ValueType<?> valueType = type.getValueType();

		if (valueType.wrapper(getContext().getIntrinsics()) == type) {
			return this.ancestor =
				valueType.typeRef(this.explicitAscendant, getScope());
		}

		return this.ancestor =
			new AncestorRef(getScope(), this.explicitAscendant).toTypeRef();
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.explicitAscendant;
	}

	@Override
	public boolean isExplicit() {
		return true;
	}

	@Override
	public Member getOverriddenMember() {
		return null;
	}

	@Override
	public StaticTypeRef getExplicitAscendant() {
		return this.explicitAscendant;
	}

	@Override
	public String toString() {
		return "ExplicitSample[" + this.explicitAscendant + ']';
	}

	@Override
	protected Obj getObject() {
		return this.explicitAscendant.getType();
	}

	private static final class AncestorRef extends Ex {

		private final StaticTypeRef explicitAscendant;

		AncestorRef(Scope scope, StaticTypeRef explicitAscendant) {
			super(explicitAscendant, scope.distribute());
			this.explicitAscendant = explicitAscendant;
		}

		@Override
		public Value<?> value(Scope scope) {

			final TypeRef ancestor = ancestor();

			if (ancestor == null) {
				return falseValue();
			}

			return ancestor.getValue();
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final StaticTypeRef typeRef = this.explicitAscendant.reproduce(reproducer);

			if (typeRef == null) {
				return null;
			}

			return new AncestorRef(reproducer.getScope(), typeRef);
		}

		@Override
		protected Resolution resolveExpression(Scope scope) {

			final TypeRef ancestor = ancestor();

			if (ancestor == null) {
				return null;
			}

			return artifactResolution(ancestor.getArtifact());
		}

		@Override
		protected RefOp createOp(HostOp host) {
			return new AncestorOp(host, this);
		}

		private TypeRef ancestor() {
			if (!this.explicitAscendant.validate()) {
				return null;
			}
			return this.explicitAscendant.getType().getAncestor();
		}

	}

	private static final class AncestorOp extends RefOp {

		private final Obj ancestor;

		AncestorOp(HostOp scope, AncestorRef ref) {
			super(scope, ref);
			this.ancestor = ref.getResolution().toObject();
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return this.ancestor.ir(getGenerator()).op(getBuilder(), code);
		}

	}

}
