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
package org.o42a.core.value.array.impl;

import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_ID;
import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.gc.GCBlock;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.util.string.ID;


public final class ArrayItemsIRContainer
		extends Struct<ArrayItemsIRContainer.Op> {

	private static final ID ITEMS_ID = ID.id("items");

	private final ArrayItemsIR items;

	public ArrayItemsIRContainer(ArrayIR arrayIR) {
		super(arrayIR.getId());
		this.items = new ArrayItemsIR(arrayIR);
	}

	@Override
	public boolean isDebuggable() {
		return false;
	}

	public final ArrayItemsIR items() {
		return this.items;
	}

	@Override
	protected void fill() {
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		data.addNewInstance(
				GC_BLOCK_ID,
				GC_BLOCK_TYPE,
				new GCBlock(this.items, "o42a_array_gc_desc"));
		data.addStruct(ITEMS_ID, this.items);
	}

	static class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
