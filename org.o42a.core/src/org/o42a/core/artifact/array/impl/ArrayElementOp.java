/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStruct;


final class ArrayElementOp extends PathOp {

	private final ArrayValueStruct arrayStruct;
	private final Ref indexRef;

	ArrayElementOp(PathOp start, ArrayValueStruct arrayStruct, Ref indexRef) {
		super(start);
		this.arrayStruct = arrayStruct;
		this.indexRef = indexRef;
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		assert !this.arrayStruct.isConstant() :
			value + " is a constant array of type " + this.arrayStruct
			+ ". Can not assign a new value to it`s item";

		final ValDirs indexDirs = dirs.value(ValueStruct.INTEGER);
		final Int64op index = loadIndex(indexDirs);

		final ValDirs arrayDirs =
				indexDirs.dirs().value(this.arrayStruct);

		assignItem(index, arrayDirs, value);

		arrayDirs.done();
		indexDirs.done();
	}

	@Override
	public HostOp target(CodeDirs dirs) {

		final ValDirs indexDirs = dirs.value(ValueStruct.INTEGER);
		final Int64op index = loadIndex(indexDirs);

		final ValDirs arrayDirs =
				indexDirs.dirs().value(this.arrayStruct);
		final ObjectOp itemObject = loadItem(arrayDirs, index);

		arrayDirs.done();
		indexDirs.done();

		return itemObject;
	}

	@Override
	public String toString() {
		if (this.indexRef == null) {
			return super.toString();
		}
		return this.arrayStruct.toString()
				+ '[' + this.indexRef.toString() + ']';
	}

	private Int64op loadIndex(ValDirs dirs) {

		final ValOp indexVal =
				this.indexRef.op(pathStart()).writeValue(dirs);
		final Code code = dirs.code();
		final Int64op index = indexVal.rawValue(
				dirs.id("idx"),
				dirs.code()).load(null, code);
		final BoolOp negativeIndex = index.lt(
				code.id("neg_idx"),
				code,
				code.int64(0));

		negativeIndex.go(code, dirs.falseDir());

		return index;
	}

	private void checkIndex(CodeDirs dirs, Int64op index, ValOp array) {

		final Code code = dirs.code();
		final Int32op length =
				array.loadLength(code.id("array_len"), code);

		length.toInt64(null, code)
		.le(null, code, index)
		.go(code, dirs.falseDir());
	}

	private AnyRecOp itemRec(ValDirs dirs, Int64op index) {

		final ValOp array =
				host().materialize(dirs.dirs()).writeValue(dirs);

		checkIndex(dirs.dirs(), index, array);

		final Code code = dirs.code();
		final AnyRecOp items =
				array.value(code.id("items_rec_ptr"), code)
				.toPtr(null, code)
				.load(code.id("items_rec"), code)
				.toPtr(null, code);

		return items.offset(
				code.id("item_rec"),
				code,
				index.toInt32(null, code));
	}

	private ObjectOp loadItem(ValDirs dirs, Int64op index) {

		final Code code = dirs.code();
		final AnyRecOp itemRec = itemRec(dirs, index);
		final AnyOp itemPtr = itemRec.load(code.id("item"), code);

		itemPtr.isNull(null, code).go(code, dirs.falseDir());

		final DataOp item = itemPtr.toData(null, code);
		final Obj itemAscendant =
				this.arrayStruct
				.getItemTypeRef()
				.typeObject(dummyUser());

		return anonymousObject(getBuilder(), item, itemAscendant);
	}

	private void assignItem(Int64op index, ValDirs arrayDirs, HostOp value) {

		final AnyRecOp itemRec = itemRec(arrayDirs, index);
		final Code code = arrayDirs.code();

		// TODO implement array element type checking.

		itemRec.store(code, value.materialize(arrayDirs.dirs()).toAny(code));
	}

}
