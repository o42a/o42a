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

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValAllocFn.VAL_ALLOC;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValAllocFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.object.Obj;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.util.string.ID;


public class ArrayConstructorBuilder
		implements FunctionBuilder<ObjectValueFn> {

	private static final ID ITEMS_ID = ID.id("items");

	private final ArrayIR arrayIR;

	public ArrayConstructorBuilder(ArrayIR arrayIR) {
		this.arrayIR = arrayIR;
	}

	@Override
	public void build(Function<ObjectValueFn> function) {

		final Array array = this.arrayIR.getArray();
		final Obj owner = array.getOwner();
		final ObjectIR ownerIR = owner.ir(function.getGenerator());
		final Block failure = function.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				ownerIR,
				ownerIR.isExact() ? EXACT : DERIVED);
		final ValOp value = function.arg(function, OBJECT_VALUE.value()).op(
				function,
				builder,
				array.getValueType(),
				VAL_TRAP);

		if (array.isEmpty()) {
			value.storeNull(function);
		} else {

			final ValDirs dirs =
					builder.dirs(function, failure.head()).value(value);

			allocateItems(dirs);

			dirs.done();
		}

		function.returnVoid();

		if (failure.exists()) {
			value.storeFalse(failure);
			failure.returnVoid();
		}
	}

	private void allocateItems(ValDirs dirs) {

		final Array array = this.arrayIR.getArray();
		final CodeDirs arrayDirs = dirs.dirs();
		final Block code = arrayDirs.code();
		final ArrayItem[] items = array.getItems();
		final FuncPtr<ValAllocFn> func =
				dirs.getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_array_alloc", VAL_ALLOC);
		AnyRecOp data = func.op(null, code)
				.allocate(dirs, items.length)
				.toRec(ITEMS_ID, code);

		allocateItem(arrayDirs, data, items[0], 0);
		for (int i = 1; i < items.length; ++i) {
			data = data.offset(null, code, code.int32(1));
			allocateItem(arrayDirs, data, items[i], i);
		}

		dirs.value().holder().hold(code);
	}

	private void allocateItem(
			CodeDirs arrayDirs,
			AnyRecOp data,
			ArrayItem item,
			int index) {

		final Block failure = arrayDirs.addBlock("item_" + index + "_failed");
		final CodeDirs dirs = arrayDirs.setFalseDir(failure.head());
		final Code code = dirs.code();
		final RefOp itemOp =
				item.getValueRef().op(dirs.getBuilder().host());
		final ObjectOp itemValue =
				itemOp.path()
				.target()
				.materialize(dirs, tempObjHolder(arrayDirs.getAllocator()));

		data.store(code, itemValue.toAny(null, code));

		dirs.done();

		if (failure.exists()) {
			data.store(failure, failure.nullPtr());
			failure.go(arrayDirs.code().tail());
		}
	}

}
