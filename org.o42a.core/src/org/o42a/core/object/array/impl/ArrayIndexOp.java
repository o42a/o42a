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
package org.o42a.core.object.array.impl;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.ID;


final class ArrayIndexOp extends StepOp<ArrayIndexStep> {

	private static final ID IDX_ID = ID.id("idx");
	private static final ID NEG_IDX_ID = ID.id("neg_idx");
	private static final ID ARRAY_LEN_ID = ID.id("array_len");
	private static final ID ITEM_ID = ID.id("item");
	private static final ID ITEM_REC_ID = ID.id("item_rec");
	private static final ID ITEMS_REC_ID = ID.id("items_rec");
	private static final ID ITEMS_REC_PTR_ID = ID.id("items_rec_ptr");

	ArrayIndexOp(PathOp start, ArrayIndexStep step) {
		super(start, step);
	}

	@Override
	public HostValueOp value() {
		return new ArrayIndexValueOp(this);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return object(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return object(dirs, tempObjHolder(dirs.getAllocator()))
				.dereference(dirs, holder);
	}

	@Override
	public HostOp target(CodeDirs dirs) {
		throw new UnsupportedOperationException(
				"Array index target does not exist");
	}

	private ObjectOp object(CodeDirs dirs, ObjHolder holder) {

		final ArrayValueStruct arrayStruct = getArrayStruct();
		final ObjectOp array = loadArray(dirs, holder);
		final ValDirs indexDirs =
				dirs.nested().value(ValueStruct.INTEGER, TEMP_VAL_HOLDER);
		final Int64op index = loadIndex(indexDirs);

		final ValDirs itemsDirs = indexDirs.dirs().nested().value(
				arrayStruct,
				TEMP_VAL_HOLDER);
		final ObjectOp itemObject = loadItem(itemsDirs, array, index);

		itemsDirs.done();
		indexDirs.done();

		return itemObject;
	}

	private ArrayValueStruct getArrayStruct() {
		return getStep()
				.getArray()
				.valueStruct(getStep().getArray().getScope())
				.toArrayStruct();
	}

	private ObjectOp loadArray(CodeDirs dirs, ObjHolder holder) {
		return getStep()
				.getArray()
				.op(host())
				.target(dirs)
				.materialize(dirs, holder);
	}

	private Int64op loadIndex(ValDirs dirs) {

		final ValOp indexVal =
				getStep().getIndex().op(pathStart()).writeValue(dirs);
		final Block code = dirs.code();
		final Int64op index = indexVal.rawValue(
				IDX_ID,
				dirs.code()).load(null, code);
		final BoolOp negativeIndex = index.lt(NEG_IDX_ID, code, code.int64(0));

		negativeIndex.go(code, dirs.falseDir());

		return index;
	}

	private void checkIndex(CodeDirs dirs, Int64op index, ValOp array) {

		final Block code = dirs.code();
		final Int32op length = array.loadLength(ARRAY_LEN_ID, code);

		length.toInt64(null, code)
		.le(null, code, index)
		.go(code, dirs.falseDir());
	}

	private AnyRecOp itemRec(ValDirs dirs, ObjectOp array, Int64op index) {

		final ValOp arrayVal = array.value().writeValue(dirs);

		checkIndex(dirs.dirs(), index, arrayVal);

		final Code code = dirs.code();
		final AnyRecOp items =
				arrayVal.value(ITEMS_REC_PTR_ID, code)
				.toPtr(null, code)
				.load(ITEMS_REC_ID, code)
				.toPtr(null, code);

		return items.offset(ITEM_REC_ID, code, index.toInt32(null, code));
	}

	private ObjectOp loadItem(ValDirs dirs, ObjectOp array, Int64op index) {

		final Block code = dirs.code();
		final AnyRecOp itemRec = itemRec(dirs, array, index);
		final AnyOp itemPtr = itemRec.load(ITEM_ID, code);

		itemPtr.isNull(null, code).go(code, dirs.falseDir());

		final DataOp item = itemPtr.toData(null, code);
		final Obj itemAscendant =
				dirs.getValueStruct()
				.toArrayStruct()
				.getItemTypeRef()
				.getType();

		return anonymousObject(getBuilder(), item, itemAscendant);
	}

	private static final class ArrayIndexValueOp implements HostValueOp {

		private final ArrayIndexOp index;

		ArrayIndexValueOp(ArrayIndexOp index) {
			this.index = index;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			object(dirs).value().writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return object(dirs.dirs()).value().writeValue(dirs);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {

			final ArrayValueStruct arrayStruct = this.index.getArrayStruct();

			assert arrayStruct.isVariable() :
				value + " is immutable of type " + arrayStruct
				+ ". Can not re-assign it`s element";

			final ObjectOp array = this.index.loadArray(
					dirs,
					tempObjHolder(dirs.getAllocator()).toVolatile());
			final ValDirs indexDirs =
					dirs.nested().value(ValueStruct.INTEGER, TEMP_VAL_HOLDER);
			final Int64op index = this.index.loadIndex(indexDirs);

			final ValDirs arrayDirs = indexDirs.dirs().nested().value(
					arrayStruct,
					TEMP_VAL_HOLDER);

			assignItem(arrayDirs, array, index, value);

			arrayDirs.done();
			indexDirs.done();
		}

		@Override
		public String toString() {
			if (this.index == null) {
				return super.toString();
			}
			return this.index.toString();
		}

		private ObjectOp object(CodeDirs dirs) {
			return this.index.object(dirs, tempObjHolder(dirs.getAllocator()));
		}

		private void assignItem(
				ValDirs dirs,
				ObjectOp array,
				Int64op index,
				HostOp value) {

			final AnyRecOp itemRec = this.index.itemRec(dirs, array, index);
			final Code code = dirs.code();

			// TODO implement array element type checking.

			final ObjectOp object = value.materialize(
					dirs.dirs(),
					tempObjHolder(dirs.getAllocator()));

			itemRec.store(code, object.toAny(null, code));
		}

	}

}
