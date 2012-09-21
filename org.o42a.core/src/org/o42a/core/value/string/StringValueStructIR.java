/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.struct.ExternalValueStructIR;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.util.DataAlignment;
import org.o42a.util.string.ID;


final class StringValueStructIR
		extends ExternalValueStructIR<SingleValueStruct<String>, String> {

	private static final ID STRING_CONST_ID = CONST_ID.sub("STRING");
	private static final ID STRING_DATA_ID = DATA_ID.sub("STRING");

	private int stringSeq;
	private int constSeq;

	StringValueStructIR(Generator generator, StringValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public KeeperIR<?, ?> createKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		return new StringKeeperIR(this, bodyIR, keeper);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return defaultValueIR(objectIR);
	}

	@Override
	protected ID constId(String value) {
		return STRING_CONST_ID.anonymous(++this.constSeq);
	}

	@Override
	protected ID valueId(String value) {
		return STRING_DATA_ID.anonymous(this.stringSeq++);
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