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
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.impl.*;
import org.o42a.core.value.array.impl.ArrayItemsIR.Op;
import org.o42a.util.string.ID;


public class ArrayIR {

	private final ArrayIRGenerator generator;
	private final ID id;
	private final Array array;
	private Val val;
	private Ptr<ValType.Op> valPtr;
	private ArrayItemsIR items;
	private FuncPtr<ObjectValFunc> constructor;

	public ArrayIR(ArrayIRGenerator generator, Array array) {
		this.generator = generator;
		this.id = generator.nextId();
		this.array = array;
	}

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
		if (this.val != null) {
			return this.val;
		}

		final Array array = getArray();

		if (array.isEmpty()) {
			return this.val = new Val(
					array.getValueType(),
					VAL_CONDITION,
					0,
					0L);
		}

		final ArrayItemsIR items = items();
		final Data<Op> itemsData = items.data(getGenerator());

		return this.val = new Val(
				array.getValueType(),
				VAL_CONDITION,
				items.length(),
				itemsData.getPointer().toAny());
	}

	public final Ptr<ValType.Op> getValPtr() {
		if (this.valPtr != null) {
			return this.valPtr;
		}

		final Global<ValType.Op, ValType> global =
				getGenerator()
				.newGlobal()
				.setConstant()
				.dontExport()
				.newInstance(CONST_ID.sub(getId()), VAL_TYPE, getVal());

		return this.valPtr = global.getPointer();
	}

	public final ArrayItemsIR items() {
		assert !getArray().isEmpty() :
			"Empty array doesn't have any items";

		if (this.items != null) {
			return this.items;
		}

		final ArrayItemsIRContainer itemsContainer =
				new ArrayItemsIRContainer(this);

		getGenerator().newGlobal().dontExport().struct(itemsContainer);

		return this.items = itemsContainer.items();
	}

	public final FuncPtr<ObjectValFunc> getConstructor() {
		if (this.constructor != null) {
			return this.constructor;
		}

		final Array origin = getArray().getOrigin();

		if (origin != getArray()) {
			return this.constructor =
					origin.ir(this.generator).getConstructor();
		}

		return this.constructor =
				getGenerator().newFunction().dontExport().create(
						getId().detail("constructor"),
						OBJECT_VAL,
						new ArrayConstructorBuilder(this))
				.getPointer();
	}

	@Override
	public String toString() {
		if (this.array == null) {
			return super.toString();
		}
		return this.array + " IR";
	}

}
