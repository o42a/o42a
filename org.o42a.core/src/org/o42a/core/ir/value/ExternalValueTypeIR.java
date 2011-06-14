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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.Val.CONDITION_FLAG;
import static org.o42a.core.ir.value.Val.EXTERNAL_FLAG;
import static org.o42a.core.ir.value.Val.STATIC_FLAG;
import static org.o42a.core.ir.value.ValUseFunc.VAL_USE;

import java.util.HashMap;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.value.ValueType;
import org.o42a.util.DataAlignment;


public abstract class ExternalValueTypeIR<T> extends AbstractValueTypeIR<T> {

	private final HashMap<T, Val> valueCache = new HashMap<T, Val>();

	public ExternalValueTypeIR(Generator generator, ValueType<T> valueType) {
		super(generator, valueType);
	}

	@Override
	public Val val(T value) {

		final Val cachedVal = this.valueCache.get(value);

		if (cachedVal != null) {
			return cachedVal;
		}

		final DataAlignment alignment = alignment(value);
		final byte[] bytes = toBinary(value, alignment);
		final Val val;

		if (bytes.length <= 8) {
			val = new Val(
					ValueType.STRING,
					CONDITION_FLAG | (alignment.getShift() << 8),
					bytes.length,
					bytesToLong(bytes));
		} else {

			final Ptr<AnyOp> binary =
				getGenerator().addBinary(valueId(value), true, bytes);

			val = new Val(
					ValueType.STRING,
					CONDITION_FLAG | EXTERNAL_FLAG | STATIC_FLAG
					| (alignment.getShift() << 8),
					bytes.length,
					binary);
		}

		this.valueCache.put(value, val);

		return val;
	}

	protected abstract CodeId valueId(T value);

	protected abstract DataAlignment alignment(T value);

	protected abstract byte[] toBinary(T value, DataAlignment alignment);

	@Override
	protected void initialize(Code code, ValOp target, ValOp value) {
		store(code, target, value);
		if (value.ptr().getAllocClass().isStatic()) {
			return;
		}
		use(code, target);
	}

	@Override
	protected void assign(Code code, ValOp target, ValOp value) {
		unuse(code, target);
		initialize(code, target, value);
	}

	private static long bytesToLong(byte[] bytes) {

		long result = 0;

		for (int i = 0; i < bytes.length; ++i) {

			final byte b = bytes[i];

			result |= (b & 0xFFL) << (i << 3);
		}

		return result;
	}

	private static void use(Code code, ValOp val) {

		final FuncPtr<ValUseFunc> func =
			code.getGenerator().externalFunction("o42a_val_use", VAL_USE);

		func.op(null, code).call(code, val);
	}

	private static void unuse(Code code, ValOp val) {

		final FuncPtr<ValUseFunc> func =
			code.getGenerator().externalFunction("o42a_val_unuse", VAL_USE);

		func.op(null, code).call(code, val);
	}

}
