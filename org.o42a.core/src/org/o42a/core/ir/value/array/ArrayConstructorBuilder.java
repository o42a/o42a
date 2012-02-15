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

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.ValAllocFunc.VAL_ALLOC;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValAllocFunc;
import org.o42a.core.ir.value.ValOp;


class ArrayConstructorBuilder implements FunctionBuilder<ObjectValFunc> {

	private final ArrayIR arrayIR;

	ArrayConstructorBuilder(ArrayIR arrayIR) {
		this.arrayIR = arrayIR;
	}

	@Override
	public void build(Function<ObjectValFunc> function) {

		final Array array = this.arrayIR.getArray();
		final Obj owner = array.getOwner();
		final ObjectIR ownerIR =
				owner.ir(function.getGenerator());
		final Block failure = function.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				ownerIR.getMainBodyIR(),
				owner,
				ownerIR.isExact() ? EXACT : DERIVED);
		final ValOp value =
				function.arg(function, OBJECT_VAL.value())
				.op(builder, array.getValueStruct())
				.setStoreMode(INITIAL_VAL_STORE);

		if (array.isEmpty()) {
			value.storeNull(function);
		} else {

			final ValDirs dirs =
					falseWhenUnknown(builder, function, failure.head())
					.value(function.id("array"), value);

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
		final Block code = dirs.code();
		final ArrayItem[] items = array.getItems();
		final FuncPtr<ValAllocFunc> func =
				dirs.getGenerator()
				.externalFunction()
				.sideEffects(false)
				.link("o42a_array_alloc", VAL_ALLOC);
		AnyRecOp data = func.op(null, code)
				.allocate(dirs, items.length)
				.toPtr(code.id("items"), code);

		allocateItem(dirs, data, items[0], 0);
		for (int i = 1; i < items.length; ++i) {
			data = data.offset(null, code, code.int32(1));
			allocateItem(dirs, data, items[i], i);
		}
	}

	private void allocateItem(
			ValDirs arrayDirs,
			AnyRecOp data,
			ArrayItem item,
			int index) {

		final Block failure = arrayDirs.addBlock("item_" + index + "_failed");
		final ValDirs dirs = falseWhenUnknown(
				arrayDirs.getBuilder(),
				arrayDirs.code(),
				failure.head()).value(arrayDirs);

		final RefOp itemOp =
				item.getValueRef().op(dirs.getBuilder().host());
		final ObjectOp itemValue =
				itemOp.target(dirs.dirs()).materialize(dirs.dirs());
		final Code code = dirs.code();

		data.store(code, itemValue.toAny(code));

		dirs.done();

		if (failure.exists()) {
			data.store(failure, failure.nullPtr());
			failure.go(arrayDirs.code().tail());
		}
	}

}
