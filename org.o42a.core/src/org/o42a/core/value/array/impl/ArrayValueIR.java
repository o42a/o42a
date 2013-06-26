/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.value.array.impl;

import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;

import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.object.Obj;
import org.o42a.core.value.Value;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


final class ArrayValueIR extends ValueIR {

	ArrayValueIR(ArrayValueTypeIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
	}

	@Override
	public void setInitialValue(ObjectTypeIR type) {

		final ValType val = type.getInstance().data().value();
		final Obj object = type.getObjectIR().getObject();
		final Value<?> value = object.value().getValue();

		if (!value.getKnowledge().isInitiallyKnown()) {
			val.set(INDEFINITE_VAL);
		} else if (value.getKnowledge().isFalse()) {
			val.set(FALSE_VAL);
		} else {

			final ArrayValueType arrayType = getValueType().toArrayType();
			final Array array =
					arrayType.cast(value).getCompilerValue();

			val.set(arrayType.ir(getGenerator()).staticsIR().val(array));
		}
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new ArrayValueOp(this, object);
	}

}
