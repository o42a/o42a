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

import static org.o42a.util.StringCodec.bytesPerChar;
import static org.o42a.util.StringCodec.escapeControlChars;
import static org.o42a.util.StringCodec.stringToBinary;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.value.ExternalValueTypeIR;
import org.o42a.core.ir.value.ValueTypeIR;
import org.o42a.core.source.Intrinsics;
import org.o42a.util.DataAlignment;


final class StringValueType extends ValueType<String> {

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
	protected ValueTypeIR<String> createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends ExternalValueTypeIR<String> {

		private int stringSeq;
		private int constSeq;

		IR(Generator generator, ValueType<String> valueType) {
			super(generator, valueType);
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
		protected CodeId constId(String value) {
			return getGenerator().id("CONST").sub("STRING")
			.anonymous(++this.constSeq);
		}

	}

}
