/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.value.array;

import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_ID;
import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_TYPE;
import static org.o42a.core.ir.gc.GCDescOp.GC_DESC_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.gc.GCBlockOp;
import org.o42a.core.ir.gc.GCBlockOp.Type;
import org.o42a.util.string.ID;


final class ArrayItemsIRContainer extends Struct<ArrayItemsIRContainer.Op> {

	private static final ArrayGCBlock ARRAY_GC_BLOCK = new ArrayGCBlock();
	private static final ID ITEMS_ID = ID.id("items");

	private final ArrayItemsIR items;

	ArrayItemsIRContainer(ArrayIR arrayIR) {
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
		data.addInstance(GC_BLOCK_ID, GC_BLOCK_TYPE, ARRAY_GC_BLOCK);
		data.addStruct(ITEMS_ID, this.items);
	}

	static class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

	private static final class ArrayGCBlock
			implements Content<GCBlockOp.Type> {

		@Override
		public void allocated(Type instance) {
		}

		@Override
		public void fill(Type instance) {
			instance.lock().setValue((byte) 0);
			instance.list().setValue((byte) 0);
			instance.flags().setValue((short) 0);
			instance.useCount().setValue(0);
			instance.desc().setConstant(true).setValue(
					instance.getGenerator()
					.externalGlobal()
					.setConstant()
					.link("o42a_array_gc_desc", GC_DESC_TYPE));
			instance.prev().setNull();
			instance.next().setNull();
		}

	}

}
