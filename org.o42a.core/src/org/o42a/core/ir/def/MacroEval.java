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
package org.o42a.core.ir.def;

import static org.o42a.core.object.macro.impl.EmptyMacro.EMPTY_MACRO;

import org.o42a.core.ir.HostOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


final class MacroEval extends InlineEval {

	static final MacroEval INSTANCE = new MacroEval();

	private MacroEval() {
		super(null);
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		dirs.returnValue(
				ValueType.MACRO
				.constantValue(EMPTY_MACRO)
				.op(dirs.getBuilder(), dirs.code()));
	}

	@Override
	public String toString() {
		return "MACRO";
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
