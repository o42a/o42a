/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.object.Obj;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


public final class ArrayItemsIR extends Struct<ArrayItemsIR.Op> {

	private final ArrayIR arrayIR;
	private final DataRec[] items;

	public ArrayItemsIR(ArrayIR arrayIR) {
		super(arrayIR.getId().detail("items"));
		this.arrayIR = arrayIR;
		// Reserve one pointer for terminator.
		this.items = new DataRec[arrayIR.getArray().getItems().length];
	}

	@Override
	public boolean isDebuggable() {
		return false;
	}

	public final int length() {
		return this.items.length;
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

		final int length = length();

		for (int i = 0; i < length; ++i) {
			this.items[i] = data.addDataPtr(Integer.toString(i));
		}
	}

	@Override
	protected void fill() {

		final Array array = this.arrayIR.getArray();
		final TypeParameters<Array> typeParameters = array.getTypeParameters();
		final ArrayValueType arrayType =
				typeParameters.getValueType().toArrayType();
		final Obj itemAscendant =
				arrayType.itemTypeRef(typeParameters).getType();
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
		public final ArrayItemsIR getType() {
			return (ArrayItemsIR) super.getType();
		}

		public DataRecOp item(Code code, int index) {
			return writer().ptr(null, code, getType().getItems()[index]);
		}
	}

}
