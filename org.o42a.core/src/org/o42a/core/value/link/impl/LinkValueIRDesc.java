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
package org.o42a.core.value.link.impl;

import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.ValueIRDesc;


public final class LinkValueIRDesc implements ValueIRDesc {

	public static final LinkValueIRDesc LINK_VALUE_IR_DESC =
			new LinkValueIRDesc();

	private LinkValueIRDesc() {
	}

	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public boolean hasLength() {
		return false;
	}

	@Override
	public ValHolder tempValHolder(ValOp value) {
		return new LinkValHolder(value);
	}

	@Override
	public ValHolder valTrap(ValOp value) {
		return new LinkValTrap(value);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
