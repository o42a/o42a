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
package org.o42a.core.ir.value.array;


import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayItem;


final class ArrayItemsStruct extends Struct<ArrayItemsStruct.Op> {

	private final ArrayIR arrayIR;
	private final DataRec[] items;

	ArrayItemsStruct(ArrayIR arrayIR) {
		super(arrayIR.getId());
		this.arrayIR = arrayIR;
		this.items = new DataRec[arrayIR.getArray().getItems().length];
	}

	@Override
	public boolean isDebuggable() {
		return false;
	}

	public final DataRec[] getItems() {
		return this.items;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		for (int i = 0; i < this.items.length; ++i) {
			this.items[i] = data.addDataPtr(Integer.toString(i));
		}
	}

	@Override
	protected void fill() {

		final Array array = this.arrayIR.getArray();
		final Obj itemAscendant =
				array.getValueStruct().getItemTypeRef().getType();
		final ArrayItem[] items = array.getItems();
		final int length = items.length;

		for (int i = 0; i < length; ++i) {

			final ArrayItem item = items[i];
			final Obj itemValue =
					item.getValueRef()
					.resolve(array.getOwner().getScope().resolver())
					.toObject();
			final ObjectIR itemValueIR = itemValue.ir(getGenerator());
			final ObjectIRBody itemBodyIR = itemValueIR.bodyIR(itemAscendant);

			this.items[i].setValue(itemBodyIR.pointer(getGenerator()).toData());
		}
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ArrayItemsStruct getType() {
			return (ArrayItemsStruct) super.getType();
		}

		public DataRecOp item(Code code, int index) {
			return writer().ptr(null, code, getType().getItems()[index]);
		}
	}

}
