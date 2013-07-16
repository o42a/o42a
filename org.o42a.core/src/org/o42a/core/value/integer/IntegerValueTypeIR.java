/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.value.integer;

import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectTypeIR;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.*;
import org.o42a.core.value.ValueType;


final class IntegerValueTypeIR extends ValueTypeIR<Long> {

	IntegerValueTypeIR(Generator generator, IntegerValueType valueType) {
		super(generator, valueType);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return new IntegerValueIR(this, objectIR);
	}

	@Override
	protected StaticsIR<Long> createStaticsIR() {
		return new IntegerStaticsIR(this);
	}

	private static final class IntegerValueIR extends SimpleValueIR {

		IntegerValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
			super(valueTypeIR, objectIR);
		}

		@Override
		public void setInitialValue(ObjectTypeIR data) {

			final long value = ValueType.INTEGER.cast(
					getObjectIR()
					.getObject()
					.value()
					.getValue()
					.getCompilerValue());

			final ValType val = data.getInstance().data().value();

			val.flags().setValue(VAL_CONDITION);
			val.length().setValue(0);
			val.value().setValue(value);
		}

	}

}
