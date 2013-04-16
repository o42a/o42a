/*
    Root Object Definition
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.root.array.IndexedItemEval.*;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


final class SetArrayItemEval extends InlineEval {

	private static final ID NEW_VAL_ID = ID.id("new_val");
	private static final ID NEW_VAL_REC_ID = ID.id("new_val_rec");

	private final SetArrayItem setItem;
	private final InlineValue inlineArray;
	private final InlineValue inlineIndex;
	private final InlineValue inlineNewValue;

	SetArrayItemEval(
			SetArrayItem setItem,
			InlineValue inlineArray,
			InlineValue inlineIndex,
			InlineValue inlineNewValue) {
		super(null);
		this.setItem = setItem;
		this.inlineArray = inlineArray;
		this.inlineIndex = inlineIndex;
		this.inlineNewValue = inlineNewValue;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final ArrayValueType arrayStruct = getArrayType();
		final ValDirs indexDirs =
				dirs.dirs().nested().value(
						ValueType.INTEGER,
						TEMP_VAL_HOLDER);
		final Int64op index = loadIndex(indexDirs, host);

		final ValDirs itemsDirs = indexDirs.dirs().nested().value(
				arrayStruct,
				TEMP_VAL_HOLDER);

		assignItem(dirs, itemsDirs, host, index);

		itemsDirs.done();
		indexDirs.done();

	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

	private final ArrayValueType getArrayType() {
		return this.setItem.getScope()
				.getEnclosingScope() // item
				.getEnclosingScope() // array
				.toObject()
				.type()
				.getValueType()
				.toArrayType();
	}

	private final LinkValueType getItemType() {
		return this.setItem.item().getValueType().toLinkType();
	}

	private Int64op loadIndex(ValDirs dirs, HostOp host) {

		final ValOp indexVal;

		if (this.inlineIndex != null) {
			indexVal = this.inlineIndex.writeValue(dirs, host);
		} else {
			indexVal = this.setItem.index().op(host).writeValue(dirs);
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

	private AnyRecOp itemRec(ValDirs dirs, HostOp host, Int64op index) {

		final ValOp arrayVal;

		if (this.inlineArray != null) {
			arrayVal = this.inlineArray.writeValue(dirs, host);
		} else {
			arrayVal = this.setItem.array().op(host).writeValue(dirs);
		}

		checkIndex(dirs.dirs(), index, arrayVal);

		final Code code = dirs.code();
		final AnyRecOp items =
				arrayVal.value(ITEMS_REC_PTR_ID, code)
				.toRec(null, code)
				.load(ITEMS_REC_ID, code)
				.toRec(null, code);

		return items.offset(ITEM_REC_ID, code, index.toInt32(null, code));
	}

	private void assignItem(
			DefDirs dirs,
			ValDirs itemsDirs,
			HostOp host,
			Int64op index) {

		final AnyRecOp itemRec = itemRec(itemsDirs, host, index);
		final ValDirs newValDirs;
		final Block code;
		final AnyOp newPtr;

		if (this.inlineNewValue != null) {
			newValDirs =
					itemsDirs.dirs().value(getItemType(), TEMP_VAL_HOLDER);
			code = newValDirs.code();

			final ValOp newVal =
					this.inlineNewValue.writeValue(newValDirs, host);

			newPtr =
					newVal.value(NEW_VAL_REC_ID, code)
					.toRec(null, code)
					.load(NEW_VAL_ID, code);
		} else {
			newValDirs = null;
			code = itemsDirs.code();

			final ObjectOp newObject =
					this.setItem.newValue()
					.op(host)
					.path()
					.target()
					.dereference(
							itemsDirs.dirs(),
							tempObjHolder(itemsDirs.getAllocator()));

			newPtr = newObject.toAny(NEW_VAL_ID, code);
		}

		itemRec.store(code, newPtr);
		dirs.returnValue(code, dirs.value().storeVoid(code));

		if (newValDirs != null) {
			newValDirs.done();
		}
	}

}
