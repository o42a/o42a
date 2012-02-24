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
package org.o42a.core.ir.value.impl;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueStruct;


public final class AllocatedValOp extends ValOp {

	private final CodeId id;
	private final AllocationCode code;
	private ValType.Op ptr;

	public AllocatedValOp(
			CodeId id,
			AllocationCode code,
			CodeBuilder builder,
			ValueStruct<?, ?> valueStruct) {
		super(builder, valueStruct);
		this.id = id;
		this.code = code;
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	@Override
	public final Val getConstant() {
		return null;
	}

	@Override
	public ValType.Op ptr() {
		if (this.ptr != null) {
			return this.ptr;
		}

		this.ptr = this.code.allocate(this.id.getLocal(), ValType.VAL_TYPE);
		storeIndefinite(this.code);

		return this.ptr;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return "(" + getValueStruct() + ") " + this.id;
	}

}
