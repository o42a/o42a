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
package org.o42a.core.value.string;

import static org.o42a.core.ir.IRNames.CONST_ID;
import static org.o42a.core.ir.IRNames.DATA_ID;
import static org.o42a.util.string.StringCodec.bytesPerChar;
import static org.o42a.util.string.StringCodec.stringToBinary;

import java.util.concurrent.atomic.AtomicInteger;

import org.o42a.core.ir.value.type.ExternStaticsIR;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.util.DataAlignment;
import org.o42a.util.string.ID;


final class StringStaticsIR extends ExternStaticsIR<String> {

	private static final ID STRING_CONST_ID = CONST_ID.sub("STRING");
	private static final ID STRING_DATA_ID = DATA_ID.sub("STRING");

	private final AtomicInteger stringSeq = new AtomicInteger();
	private final AtomicInteger constSeq = new AtomicInteger();

	StringStaticsIR(ValueTypeIR<String> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	protected ID constId(String value) {
		return STRING_CONST_ID.anonymous(this.constSeq.incrementAndGet());
	}

	@Override
	protected ID valueId(String value) {
		return STRING_DATA_ID.anonymous(this.stringSeq.incrementAndGet());
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
