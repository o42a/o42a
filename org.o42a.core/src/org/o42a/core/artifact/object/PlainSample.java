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
import org.o42a.core.artifact.Directive;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ex;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class PlainSample extends Sample {

	private final StaticTypeRef sampleRef;
	private final StaticTypeRef explicitAscendant;
	private Obj object;
	private TypeRef ancestor;

	PlainSample(
			final Scope scope,
			final StaticTypeRef sampleRef,
			final boolean explicit) {
		super(sampleRef, scope);
		this.sampleRef = sampleRef;
		this.explicitAscendant = explicit ? sampleRef : null;
		assertSameScope(sampleRef);
	}

	@Override
	public TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}

		final Obj type = this.sampleRef.getType();

		if (!isExplicit()) {
			return this.ancestor = type.getAncestor().upgradeScope(getScope());
		}

		final ValueType<?> valueType = type.getValueType();

		if (valueType.wrapper(getContext().getIntrinsics()) == type) {
			return this.ancestor =
				valueType.typeRef(this.sampleRef, getScope());
		}

		return this.ancestor =
			new AncestorEx(getScope(), this.sampleRef).toTypeRef();
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.sampleRef;
	}

	@Override
	public boolean isExplicit() {
		return this.explicitAscendant != null;
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
	public Directive toDirective() {
		return getObject().toDirective();
	}

	@Override
	public void inheritMembers(ObjectMembers members) {
		members.deriveMembers(getObject());
	}

	@Override
	public Definitions overrideDefinitions(
			Scope scope,
			Definitions ancestorDefinitions) {

		final Obj object = getObject();
		final Definitions overriddenDefinitions =
			object.overriddenDefinitions(scope, ancestorDefinitions);

		return object.overrideDefinitions(scope, overriddenDefinitions);
	}

	@Override
	public String toString() {
		if (this.explicitAscendant == null) {
			return "ImplicitSample[" + this.sampleRef + ']';
		}
		return "ExplicitSample[" + this.explicitAscendant + ']';
	}

	private Obj getObject() {
		if (this.object == null) {
			this.object = this.sampleRef.getType();
		}
		return this.object;
	}

	private static final class AncestorEx extends Ex {

		private final StaticTypeRef sampleRef;

		AncestorEx(Scope scope, StaticTypeRef sampleRef) {
			super(sampleRef, scope.distribute());
			this.sampleRef = sampleRef;
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

			final StaticTypeRef typeRef = this.sampleRef.reproduce(reproducer);

			if (typeRef == null) {
				return null;
			}

			return new AncestorEx(reproducer.getScope(), typeRef);
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
			if (!this.sampleRef.validate()) {
				return null;
			}
			return this.sampleRef.getType().getAncestor();
		}

	}

	private static final class AncestorOp extends RefOp {

		private final Obj ancestor;

		AncestorOp(HostOp scope, AncestorEx ref) {
			super(scope, ref);
			this.ancestor = ref.getResolution().toObject();
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return this.ancestor.ir(getGenerator()).op(getBuilder(), code);
		}

	}

}
