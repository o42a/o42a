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
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Struct<O extends PtrOp> extends Type<O> {

	public Struct(String id) {
		super(id);
	}

	protected abstract void fill();

	final void setStruct(String name) {
		this.data = new StructData<O>(this, name);
	}

	final void setGlobal(Global<O, ?> global) {
		this.data = new GlobalData<O>(this, global);
	}

	private static final class StructData<O extends PtrOp> extends SubData<O> {

		StructData(Struct<O> type, String name) {
			super(name, type.getId(), type);
		}

		@Override
		public String toString() {
			return getType().toString();
		}

		@Override
		protected void allocate(Generator generator) {
			setGenerator(generator);

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.enter(getType().allocation(), this));
			getType().allocate(this);
			allocator.exit(this);
		}

		@Override
		protected void write(DataWriter writer) {
			writer.enter(getPointer().getAllocation(), this);
			((Struct<O>) getType()).fill();
			writeIncluded(writer);
			writer.exit(getPointer().getAllocation(), this);
		}

	}

	private static final class GlobalData<O extends PtrOp> extends SubData<O> {

		private final Global<O, ?> global;

		public GlobalData(Type<O> type, Global<O, ?> global) {
			super(global.getId(), global.getId(), type);
			this.global = global;
		}

		@Override
		protected void allocate(Generator generator) {
			setGenerator(generator);

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.begin(getType().allocation(), this.global));
			getType().allocate(this);
			allocator.end(this.global);
		}

		@Override
		protected void write(DataWriter writer) {
			writer.begin(getPointer().getAllocation(), this.global);
			((Struct<O>) getType()).fill();
			writeIncluded(writer);
			writer.end(getPointer().getAllocation(), this.global);
		}

	}

}
