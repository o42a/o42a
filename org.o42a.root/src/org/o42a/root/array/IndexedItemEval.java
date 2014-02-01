/*
    Root Object Definition
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
package org.o42a.root.array;

import static org.o42a.core.ir.object.op.ObjHolder.useVar;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


final class IndexedItemEval extends InlineEval {

	static final ID IDX_ID = ID.id("idx");
	static final ID NEG_IDX_ID = ID.id("neg_idx");
	static final ID ARRAY_LEN_ID = ID.id("array_len");
	static final ID ITEM_ID = ID.id("item");
	static final ID ITEM_REC_ID = ID.id("item_rec");
	static final ID ITEMS_REC_ID = ID.id("items_rec");
	static final ID ITEMS_REC_PTR_ID = ID.id("items_rec_ptr");

	private final IndexedItem item;
	private final InlineValue inlineArray;
	private final InlineValue inlineIndex;
	private final boolean row;

	IndexedItemEval(
			IndexedItem item,
			InlineValue inlineArray,
			InlineValue inlineIndex,
			boolean row) {
		super(null);
		this.item = item;
		this.inlineArray = inlineArray;
		this.inlineIndex = inlineIndex;
		this.row = row;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final ArrayValueType arrayType = getArrayType();
		final ValDirs indexDirs =
				dirs.dirs().nested().value(
						ValueType.INTEGER,
						TEMP_VAL_HOLDER);
		final Int64op index = loadIndex(indexDirs, host);

		final ValDirs itemsDirs = indexDirs.dirs().nested().value(
				arrayType,
				TEMP_VAL_HOLDER);

		loadItem(dirs, itemsDirs, host, index);

		itemsDirs.done();
		indexDirs.done();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

	private final ArrayValueType getArrayType() {
		return this.item.getScope()
				.getEnclosingScope()
				.toObject()
				.type()
				.getValueType()
				.toArrayType();
	}

	private Int64op loadIndex(ValDirs dirs, HostOp host) {

		final ValOp indexVal;

		if (this.inlineIndex != null) {
			indexVal = this.inlineIndex.writeValue(dirs, host);
		} else {
			indexVal = this.item.index().op(host).writeValue(dirs);
		}

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

	private DataRecOp itemRec(ValDirs dirs, HostOp host, Int64op index) {

		final ValOp arrayVal;

		if (this.inlineArray != null) {
			arrayVal = this.inlineArray.writeValue(dirs, host);
		} else {
			arrayVal = this.item.array().op(host).writeValue(dirs);
		}

		checkIndex(dirs.dirs(), index, arrayVal);

		final Code code = dirs.code();
		final DataRecOp items =
				arrayVal.value(ITEMS_REC_PTR_ID, code)
				.toRec(null, code)
				.load(ITEMS_REC_ID, code)
				.toDataRec(null, code);

		return items.offset(ITEM_REC_ID, code, index.toInt32(null, code));
	}

	private void loadItem(
			DefDirs dirs,
			ValDirs itemsDirs,
			HostOp host,
			Int64op index) {

		final Block code = itemsDirs.code();
		final DataRecOp itemRec = itemRec(itemsDirs, host, index);
		final DataOp itemPtr;

		if (this.row) {
			itemPtr = itemRec.load(null, code);
		} else {
			itemPtr = useVar(code, itemRec);
		}

		itemPtr.isNull(null, code).go(code, itemsDirs.falseDir());

		final ValOp value = dirs.value();

		if (this.row) {
			value.store(code, itemPtr.toAny(null, code));
		} else {
			value.set(code, itemPtr.toAny(null, code));
		}

		dirs.returnValue(code, value);
	}

}
