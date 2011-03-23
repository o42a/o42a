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

import static org.o42a.core.ir.op.Val.CONDITION_FLAG;
import static org.o42a.core.ir.op.Val.EXTERNAL_FLAG;
import static org.o42a.util.StringCodec.bytesPerChar;
import static org.o42a.util.StringCodec.escapeControlChars;
import static org.o42a.util.StringCodec.stringToBinary;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.op.Val;
import org.o42a.util.DataAlignment;


final class StringValueType extends ValueType<String> {

	private static Generator cacheGenerator;
	private static final HashMap<String, Val> cache =
		new HashMap<String, Val>();
	private static int seq;

	StringValueType() {
		super("string", String.class);
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getString();
	}

	@Override
	public String valueString(String value) {

		final StringBuilder out = new StringBuilder(value.length() + 2);

		out.append('"');
		escapeControlChars(out, value);
		out.append('"');

		return out.toString();
	}

	@Override
	protected Val val(Generator generator, String value) {

		final Val cachedVal = cachedVal(generator, value);

		if (cachedVal != null) {
			return cachedVal;
		}

		final DataAlignment bytesPerChar = bytesPerChar(value);
		final byte[] bytes = new byte[bytesPerChar.getBytes() * value.length()];

		stringToBinary(value, bytes, bytesPerChar);

		final Val val;

		if (bytes.length <= 8) {
			val = new Val(
					CONDITION_FLAG | (bytesPerChar.getShift() << 8),
					bytes.length,
					bytesToLong(bytes));
		} else {

			final Ptr<AnyOp> binary =
				generator.addBinary(
						generator.id("STRING_" + (seq++)),
						bytes);

			val = new Val(
					CONDITION_FLAG | EXTERNAL_FLAG
					| (bytesPerChar.getShift() << 8),
					bytes.length,
					binary);
		}

		cache.put(value, val);

		return val;
	}

	private static Val cachedVal(Generator generator, String string) {
		if (generator == cacheGenerator) {
			return cache.get(string);
		}

		cacheGenerator = generator;
		cache.clear();
		seq = 0;

		return null;
	}

	private static long bytesToLong(byte[] bytes) {

		long result = 0;

		for (int i = 0; i < bytes.length; ++i) {

			final byte b = bytes[i];

			result |= (b & 0xFFL) << (i << 3);
		}

		return result;
	}

}
