/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Functions;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Globals extends Functions {

	private final DataChain globals = new DataChain();

	public byte getWideCharSize() {
		return dataAllocator().getWideCharSize();
	}

	public final Ptr<AnyOp> addBinary(String id, byte[] data) {
		return new Ptr<AnyOp>(
				dataAllocator().addBinary(id, data, 0, data.length));
	}

	public Ptr<AnyOp> addBinary(String id, byte[] data, int start, int end) {
		return new Ptr<AnyOp>(dataAllocator().addBinary(id, data, start, end));
	}

	public final <T extends Type<?>> T addType(T type) {
		type.setType();

		final SubData<?> data = type.getTypeData();

		data.allocate((Generator) this);
		addType(data);

		return type;
	}

	public final GlobalSettings newGlobal() {
		return new GlobalSettings(this);
	}

	public abstract DataAllocator dataAllocator();

	public abstract DataWriter dataWriter();

	public int stringToBinary(String string, byte[] bytes) {

		final int length = string.length();

		if (length == 0) {
			return 0;
		}

		final byte wcharSize = getWideCharSize();
		int b = 0;
		int i = 0;

		do {

			final int c = string.codePointAt(i);
			final int charCount = Character.charCount(c);

			i += charCount;

			bytes[b] = (byte) c;
			bytes[b + 1] = (byte) ((c & 0xFF00) >>> 8);
			if (charCount > 1 && wcharSize > 2) {
				bytes[b + 2] = (byte) ((c & 0xFF0000) >>> 16);
				bytes[b + 3] = (byte) ((c & 0xFF000000) >>> 24);
			}

			b += wcharSize;
		} while (i < length);

		return b;
	}

	public byte[] stringToBinary(String string) {

		final int size = string.length() * getWideCharSize();
		final byte[] bytes = new byte[size];
		final int written = stringToBinary(string, bytes);

		if (written == size) {
			return bytes;
		}

		final byte[] result = new byte[written];

		System.arraycopy(bytes, 0, result, 0, written);

		return result;
	}

	@Override
	protected void writeData() {

		final DataWriter writer = dataWriter();
		Data<?> global = this.globals.getFirst();

		while (global != null) {
			global.write(writer);
			global = global.getNext();
		}

		this.globals.empty();
	}

	protected void addType(SubData<?> type) {
	}

	protected void addGlobal(SubData<?> global) {
		this.globals.add(global);
	}

	<O extends PtrOp, T extends Type<O>> Global<O, T> addGlobal(
			GlobalSettings settings,
			String id,
			T type,
			Content<T> content) {

		final Global<O, T> global =
			new Global<O, T>(settings, id, type, content);
		final SubData<O> data = global.getInstance().getTypeData();

		data.allocate((Generator) this);
		addGlobal(global.getInstance().getTypeData());

		return global;
	}

	<O extends PtrOp, S extends Struct<O>> Global<O, S> addGlobal(
			GlobalSettings settings,
			S struct) {

		final Global<O, S> global = new Global<O, S>(settings, struct);

		struct.setGlobal(global);

		final SubData<O> data = global.getInstance().getTypeData();

		data.allocate((Generator) this);
		addType(global.getInstance().getTypeData());
		addGlobal(global.getInstance().getTypeData());

		return global;
	}

}
