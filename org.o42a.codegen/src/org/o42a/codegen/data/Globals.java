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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Functions;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Globals extends Functions {

	private final DataChain globals = new DataChain();

	public final Ptr<AnyOp> addBinary(CodeId id, byte[] data) {
		return new Ptr<AnyOp>(
				dataAllocator().addBinary(id, data, 0, data.length));
	}

	public Ptr<AnyOp> addBinary(CodeId id, byte[] data, int start, int end) {
		return new Ptr<AnyOp>(dataAllocator().addBinary(id, data, start, end));
	}

	public final <T extends Type<?>> T addType(T type) {
		type.setType((Generator) this);

		final SubData<?> data = type.getTypeData();

		data.allocateData((Generator) this);

		return type;
	}

	public final GlobalSettings newGlobal() {
		return new GlobalSettings(this);
	}

	public abstract DataAllocator dataAllocator();

	public abstract DataWriter dataWriter();

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
			CodeId id,
			T type,
			Content<T> content) {

		final Global<O, T> global =
			new Global<O, T>(settings, id, type, content);
		final SubData<O> data = global.getInstance().getTypeData();

		data.allocateData((Generator) this);

		return global;
	}

	<O extends PtrOp, S extends Struct<O>> Global<O, S> addGlobal(
			GlobalSettings settings,
			S struct) {

		final Global<O, S> global = new Global<O, S>(settings, struct);

		struct.setGlobal(global);

		final SubData<O> data = global.getInstance().getTypeData();

		data.allocateData((Generator) this);

		return global;
	}

}
