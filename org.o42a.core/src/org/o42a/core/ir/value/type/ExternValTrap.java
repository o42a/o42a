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

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;


public class ExternValTrap extends ValHolder {

	public ExternValTrap(ValOp value) {
		super(value);
	}

	@Override
	public boolean holdable(ValOp value) {
		return !value.isConstant();
	}

	@Override
	protected void setValue(Code code, ValOp value) {
	}

	@Override
	protected void holdValue(Code code, ValOp value) {
		value.useRefCounted(code);
	}

}
