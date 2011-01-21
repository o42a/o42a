/*
    Intrinsics
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
package org.o42a.intrinsic.numeric;

import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.PathBuilder.pathBuilder;
import static org.o42a.core.st.Conditions.emptyConditions;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathBuilder;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ByString<T> extends IntrinsicObject {

	private static final PathBuilder BY_STRING =
		pathBuilder("adapters", "by_string");
	private static final PathBuilder INPUT =
		BY_STRING.appendName("input");

	ByString(Obj owner, ValueType<T> valueType) {
		super(
				fieldDeclaration(
						owner,
						owner.distribute(),
						BY_STRING.toAdapterId(owner, owner.distribute()))
				.prototype()
				.override()
				.setDeclaredIn(owner.getAncestor().toStatic()));
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

		final Ref selfOrDerived = selfOrDerived();

		selfOrDerived.setConditions(emptyConditions(this));

		return selfOrDerived.define(new DefinitionTarget(getScope()));
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

	protected abstract T byString(LocationSpec location, String input);

	protected abstract void parse(
			IRGenerator generator,
			Code code,
			ValOp result,
			ValOp input);

	private final class ValueIR extends ProposedValueIR {

		public ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final CodeBlk cantParse = code.addBlock("cant_parse");
			final ObjectOp input =
				host.field(
						code,
						cantParse.head(),
						INPUT.memberOf(getScope()).getKey())
				.materialize(code, cantParse.head());
			final ValOp value =
				code.allocate(getGenerator().valType()).storeUnknown(code);

			input.writeValue(code, value);

			parse(getGenerator(), code, result, value);
			code.returnVoid();

			if (cantParse.exists()) {
				result.storeFalse(cantParse);
				cantParse.returnVoid();
			}
		}

	}

}
