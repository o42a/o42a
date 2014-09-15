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
package org.o42a.core.ir.value.array;

import static org.o42a.core.ir.IRNames.CONST_ID;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Codegen;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.impl.*;
import org.o42a.core.value.array.impl.ArrayItemsIR.Op;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public class ArrayIR implements Codegen {

	private final ArrayIRGenerator generator;
	private final ID id;
	private final Array array;
	private final Init<Val> val = init(this::createVal);
	private final Init<Ptr<ValType.Op>> valPtr = init(this::allocateVal);
	private final Init<ArrayItemsIR> items = init(this::allocateItems);
	private final Init<FuncPtr<ObjectValueFn>> constructor =
			init(this::createConstructor);

	public ArrayIR(ArrayIRGenerator generator, Array array) {
		this.generator = generator;
		this.id = generator.nextId();
		this.array = array;
	}

	@Override
	public final Generator getGenerator() {
		return this.generator.getGenerator();
	}

	public final Array getArray() {
		return this.array;
	}

	public final ID getId() {
		return this.id;
	}

	public final Val getVal() {
		return this.val.get();
	}

	public final Ptr<ValType.Op> getValPtr() {
		return this.valPtr.get();
	}

	public final ArrayItemsIR items() {
		return this.items.get();
	}

	public final FuncPtr<ObjectValueFn> getConstructor() {
		return this.constructor.get();
	}

	@Override
	public String toString() {
		if (this.array == null) {
			return super.toString();
		}
		return this.array + " IR";
	}

	private Val createVal() {

		final Array array = getArray();

		if (array.isEmpty()) {
			return new Val(
					array.getValueType(),
					VAL_CONDITION,
					0,
					0L);
		}

		final ArrayItemsIR items = items();
		final Data<Op> itemsData = items.data(getGenerator());

		return new Val(
				array.getValueType(),
				VAL_CONDITION,
				items.length(),
				itemsData.getPointer().toAny());
	}

	private Ptr<ValType.Op> allocateVal() {
		final Global<ValType.Op, ValType> global =
				getGenerator()
				.newGlobal()
				.setConstant()
				.dontExport()
				.newInstance(CONST_ID.sub(getId()), VAL_TYPE, getVal());

		return global.getPointer();
	}

	private ArrayItemsIR allocateItems() {
		assert !getArray().isEmpty() :
			"Empty array doesn't have any items";

		final ArrayItemsIRContainer itemsContainer =
				new ArrayItemsIRContainer(this);

		getGenerator().newGlobal().dontExport().struct(itemsContainer);

		return itemsContainer.items();
	}

	private FuncPtr<ObjectValueFn> createConstructor() {

		final Array origin = getArray().getOrigin();

		if (origin != getArray()) {
			return origin.ir(this.generator).getConstructor();
		}

		return getGenerator().newFunction().dontExport().create(
				getId().detail("constructor"),
				OBJECT_VALUE,
				new ArrayConstructorBuilder(this))
		.getPointer();
	}

}
