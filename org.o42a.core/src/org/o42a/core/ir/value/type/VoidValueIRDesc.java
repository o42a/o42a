/*
    Compiler Core
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
package org.o42a.core.ir.value.type;

import static org.o42a.core.ir.value.ValHolder.NO_VAL_HOLDER;

import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;


final class VoidValueIRDesc implements ValueIRDesc {

	static final VoidValueIRDesc INSTANCE = new VoidValueIRDesc();

	private VoidValueIRDesc() {
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public boolean hasLength() {
		return false;
	}

	@Override
	public ValHolder tempValHolder(ValOp value) {
		return NO_VAL_HOLDER;
	}

	@Override
	public ValHolder valTrap(ValOp value) {
		return NO_VAL_HOLDER;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
