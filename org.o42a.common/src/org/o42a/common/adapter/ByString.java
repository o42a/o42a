/*
    Modules Commons
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
package org.o42a.common.adapter;

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.PathBuilder.pathBuilder;
import static org.o42a.core.st.StatementEnv.defaultEnv;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathBuilder;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ByString<T> extends IntrinsicObject {

	public static final PathBuilder BY_STRING =
		pathBuilder("adapters", "by_string");
	public static final PathBuilder INPUT =
		BY_STRING.appendName("input");

	public ByString(Obj owner, ValueType<T> valueType) {
		this(
				fieldDeclaration(
						owner,
						owner.distribute(),
						BY_STRING.toAdapterId(owner, owner.distribute()))
				.prototype()
				.override(),
				valueType);
	}

	public ByString(FieldDeclaration declaration, ValueType<T> valueType) {
		super(declaration);
		setValueType(valueType);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref self = selfRef();

		self.setEnv(defaultEnv(this));

		return self.define(getScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Field<?> inputField = INPUT.fieldOf(scope);
		final Value<?> inputValue =
			inputField.getArtifact().materialize().getValue();

		if (!inputValue.isDefinite()) {
			return getValueType().runtimeValue();
		}
		if (inputValue.isFalse()) {
			return getValueType().falseValue();
		}

		final String input =
			ValueType.STRING.cast(inputValue).getDefiniteValue();
		final T result = byString(inputField, input);

		if (result == null) {
			return getValueType().falseValue();
		}

		@SuppressWarnings("unchecked")
		final ValueType<T> valueType = (ValueType<T>) getValueType();

		return valueType.definiteValue(result);
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	protected abstract T byString(LocationInfo location, String input);

	protected abstract void parse(Code code, ValOp result, ObjectOp input);

	private final class ValueIR extends ProposedValueIR {

		public ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final CodeBlk cantParse = code.addBlock("cant_parse");
			final CodeDirs dirs = falseWhenUnknown(code, cantParse.head());
			final ObjectOp input =
				host.field(dirs, INPUT.memberOf(getScope()).getKey())
				.materialize(dirs);

			parse(code, result, input);
			if (cantParse.exists()) {
				result.storeFalse(cantParse);
				cantParse.go(code.tail());
			}
		}

	}

}
