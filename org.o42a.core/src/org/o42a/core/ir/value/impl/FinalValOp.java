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

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueStruct;


public final class FinalValOp extends ValOp {

	private final ValType.Op ptr;

	public FinalValOp(
			CodeBuilder builder,
			ValType.Op ptr,
			ValueStruct<?, ?> valueStruct) {
		super(builder, valueStruct);
		this.ptr = ptr;
	}

	@Override
	public final Val getConstant() {
		return null;
	}

	@Override
	public final ValType.Op ptr() {
		return this.ptr;
	}

	@Override
	public String toString() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		if (valueStruct == null) {
			return super.toString();
		}

		return "(" + valueStruct + ") " + ptr();
	}

}
