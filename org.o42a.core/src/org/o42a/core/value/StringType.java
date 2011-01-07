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

import static org.o42a.codegen.data.DataLayout.alignmentShift;
import static org.o42a.core.ir.op.Val.CONDITION_FLAG;
import static org.o42a.core.ir.op.Val.EXTERNAL_FLAG;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.Val;


final class StringType extends ValueType<String> {

	private static Generator cacheGenerator;
	private static final HashMap<String, Val> cache =
		new HashMap<String, Val>();
	private static int seq;

	StringType() {
		super("string", String.class);
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getString();
	}

	@Override
	protected Val val(IRGenerator generator, String value) {

		final Val cachedVal = cachedVal(generator, value);

		if (cachedVal != null) {
			return cachedVal;
		}

		final byte wcharSize = generator.getGenerator().getWideCharSize();
		final byte[] bytes = generator.getGenerator().stringToBinary(value);

		final Val val;

		if (bytes.length <= 8) {
			val = new Val(
					CONDITION_FLAG | (alignmentShift(wcharSize) << 8),
					bytes.length,
					bytesToLong(bytes));
		} else {

			final Ptr<AnyOp> binary =
				generator.getGenerator().addBinary("STRING_" + (seq++), bytes);

			val = new Val(
					CONDITION_FLAG | EXTERNAL_FLAG
					| (alignmentShift(wcharSize) << 8),
					bytes.length,
					binary);
		}

		cache.put(value, val);

		return val;
	}

	private static Val cachedVal(IRGenerator generator, String string) {

		final Generator gen = generator.getGenerator();

		if (gen != cacheGenerator) {
			cacheGenerator = gen;
			cache.clear();
			seq = 0;
			return null;
		}

		return cache.get(string);
	}

	private static long bytesToLong(byte[] bytes) {

		long result = 0;

		for (int i = 0; i < bytes.length; ++i) {

			final byte b = bytes[i];

			result |= (b & 0xFFL) << i;
		}

		return result;
	}

}
