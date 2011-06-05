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
package org.o42a.core.value;

import static org.o42a.core.ir.op.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.op.Val;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ir.op.ValOp.Type;


final class RuntimeValue<T> extends Value<T> {

	private static Ptr<ValOp> cachedPtr;
	private static Generator cachedGenerator;

	RuntimeValue(ValueType<T> valueType) {
		super(valueType);
	}

	@Override
	public T getDefiniteValue() {
		return null;
	}

	@Override
	public LogicalValue getLogicalValue() {
		return LogicalValue.RUNTIME;
	}

	@Override
	public Val val(Generator generator) {
		return INDEFINITE_VAL;
	}

	@Override
	public Ptr<ValOp> valPtr(Generator generator) {
		if (cachedPtr != null && cachedGenerator == generator) {
			return cachedPtr;
		}
		cachedGenerator = generator;

		final Global<ValOp, Type> global =
			generator.newGlobal().setConstant().dontExport().newInstance(
					generator.id("CONST").sub("INDEFINITE"),
					VAL_TYPE,
					INDEFINITE_VAL);

		return cachedPtr = global.getPointer();
	}

	@Override
	public String toString() {
		return '(' + getValueType().toString() + ") runtime";
	}

}
