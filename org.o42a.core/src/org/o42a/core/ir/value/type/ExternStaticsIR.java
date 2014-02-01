/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value.type;

import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_EXTERNAL;
import static org.o42a.core.ir.value.Val.VAL_STATIC;

import java.util.HashMap;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.util.DataAlignment;
import org.o42a.util.string.ID;


public abstract class ExternStaticsIR<T> extends CachingStaticsIR<T> {

	private final HashMap<T, Val> valueCache = new HashMap<>();

	public ExternStaticsIR(ValueTypeIR<T> valueTypeIR) {
		super(valueTypeIR);
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
					getValueType(),
					VAL_CONDITION | (alignment.getShift() << 8),
					length(value, bytes, alignment),
					bytesToLong(bytes));
		} else {

			final Ptr<AnyOp> binary =
					getGenerator().addBinary(valueId(value), true, bytes);

			val = new Val(
					getValueType(),
					VAL_CONDITION | VAL_EXTERNAL | VAL_STATIC
					| (alignment.getShift() << 8),
					length(value, bytes, alignment),
					binary);
		}

		this.valueCache.put(value, val);

		return val;
	}

	protected abstract ID valueId(T value);

	protected abstract DataAlignment alignment(T value);

	protected abstract byte[] toBinary(T value, DataAlignment alignment);

	protected abstract int length(
			T value,
			byte[] binary,
			DataAlignment alignment);

	private static long bytesToLong(byte[] bytes) {

		long result = 0;

		for (int i = 0; i < bytes.length; ++i) {

			final byte b = bytes[i];

			result |= (b & 0xFFL) << (i << 3);
		}

		return result;
	}

}
