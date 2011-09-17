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
package org.o42a.core.value.impl;

import static org.o42a.util.string.StringCodec.bytesPerChar;
import static org.o42a.util.string.StringCodec.escapeControlChars;
import static org.o42a.util.string.StringCodec.stringToBinary;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.ExternalValueStructIR;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.DataAlignment;


public class StringValueStruct extends SingleValueStruct<String> {

	public static final StringValueStruct INSTANCE = new StringValueStruct();

	private StringValueStruct() {
		super(ValueType.STRING, String.class);
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
	protected ValueStructIR<SingleValueStruct<String>, String> createIR(
			Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR
			extends ExternalValueStructIR<SingleValueStruct<String>, String> {

		private int stringSeq;
		private int constSeq;

		IR(Generator generator, StringValueStruct valueStruct) {
			super(generator, valueStruct);
		}

		@Override
		protected CodeId constId(String value) {
			return getGenerator().id("CONST").sub("STRING")
					.anonymous(++this.constSeq);
		}

		@Override
		protected CodeId valueId(String value) {
			return getGenerator().id("STRING_" + (this.stringSeq++));
		}

		@Override
		protected DataAlignment alignment(String value) {
			return bytesPerChar(value);
		}

		@Override
		protected byte[] toBinary(String value, DataAlignment alignment) {

			final byte[] bytes =
					new byte[alignment.getBytes() * value.length()];

			stringToBinary(value, bytes, alignment);

			return bytes;
		}

		@Override
		protected int length(
				String value,
				byte[] binary,
				DataAlignment alignment) {
			return binary.length >>> alignment.getShift();
		}

	}

}
