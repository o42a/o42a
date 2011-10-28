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
package org.o42a.core.ir.value.array;

import static org.o42a.core.ir.value.Val.CONDITION_FLAG;
import static org.o42a.core.ir.value.Val.EXTERNAL_FLAG;
import static org.o42a.core.ir.value.Val.STATIC_FLAG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.data.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.array.ArrayItemsStruct.Op;


public class ArrayIR {

	private final Generator generator;
	private final CodeId id;
	private final Array array;
	private Val val;
	private Ptr<ValType.Op> valPtr;
	private ArrayItemsStruct items;

	public ArrayIR(ArrayIRGenerator generator, Array array) {
		this.generator = generator.getGenerator();
		this.id = generator.nextId();
		this.array = array;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Array getArray() {
		return this.array;
	}

	public final CodeId getId() {
		return this.id;
	}

	public final Val getVal() {
		if (this.val != null) {
			return this.val;
		}

		final ArrayItemsStruct items = getItems();
		final Data<Op> itemsData = items.data(getGenerator());
		final DataLayout itemsLayout = itemsData.getLayout();

		return this.val = new Val(
				getArray().getValueStruct(),
				CONDITION_FLAG | EXTERNAL_FLAG | STATIC_FLAG
				| (itemsLayout.getAlignmentShift() << 8),
				items.getItems().length,
				itemsData.getPointer().toAny());
	}

	public final Ptr<ValType.Op> getValPtr() {
		if (this.valPtr != null) {
			return this.valPtr;
		}

		final Global<ValType.Op, ValType> global =
				getGenerator().newGlobal().setConstant().dontExport()
				.newInstance(
						getGenerator().id("CONST").sub(getId()),
						ValType.VAL_TYPE,
						getVal());

		return this.valPtr = global.getPointer();
	}

	public final ArrayItemsStruct getItems() {
		if (this.items != null) {
			return this.items;
		}

		this.items = new ArrayItemsStruct(this);
		getGenerator()
		.newGlobal()
		.dontExport()
		.setConstant(getArray().isConstant())
		.allocateStruct(this.items);

		return this.items;
	}

	@Override
	public String toString() {
		if (this.array == null) {
			return super.toString();
		}
		return this.array + " IR";
	}

}
