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
package org.o42a.core.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.common.Expression;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class AncestorRef extends Expression {

	private final Ref ref;
	private boolean error;

	AncestorRef(LocationInfo location, Ref ref) {
		super(location, ref.distribute());
		this.ref = ref;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Value<?> value(Scope scope) {

		final TypeRef ancestor = ancestor(scope);

		if (ancestor == null) {
			return Value.unknownValue();
		}

		return ancestor.getValue();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new AncestorRef(reproducer.getScope(), ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref + "^^";
	}

	@Override
	protected Resolution resolveExpression(Scope scope) {

		final TypeRef ancestor = ancestor(scope);

		if (ancestor == null) {
			return null;
		}

		return artifactResolution(ancestor.getArtifact());
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new AncestorOp(host, this);
	}

	private TypeRef ancestor(Scope scope) {
		if (this.error) {
			return null;
		}

		final Resolution resolution = this.ref.resolve(scope);

		if (resolution.isError()) {
			this.error = true;
			return null;
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			this.error = true;
			getLogger().notArtifact(resolution);
			return null;
		}

		final Obj object = artifact.toObject();

		if (object != null) {

			final ValueType<?> valueType = object.getValueType();

			if (valueType.wrapper(getContext().getIntrinsics()) == object) {
				return valueType.typeRef(
						this,
						object.getScope().getEnclosingScope());
			}

			return object.getAncestor();
		}

		final TypeRef typeRef = artifact.getTypeRef();

		if (typeRef != null) {
			return typeRef;
		}

		this.error = true;
		getLogger().error(
				"no_ancestor",
				this,
				"Artifact %s has no ancestor",
				artifact.getKind());

		return null;
	}


	private static final class AncestorOp extends RefOp {

		AncestorOp(HostOp scope, AncestorRef ref) {
			super(scope, ref);
		}

		@Override
		public HostOp target(Code code, CodePos exit) {

			final AncestorRef ref = (AncestorRef) getRef();
			final ObjectOp object =
				ref.ref.op(host()).target(code, exit).materialize(code, exit);
			final ObjectTypeOp ancestorData =
				object.objectType(code).ptr()
				.data(code)
				.ancestorType(code)
				.load(code)
				.op(getBuilder(), object.getPrecision());

			return ancestorData.object(code, ref.getResolution().materialize());
		}

	}

}
