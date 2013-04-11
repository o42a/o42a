/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.util.fn.Cancelable;


final class InlineValueEval extends InlineEval {

	private final InlineValue value;

	InlineValueEval(InlineValue value) {
		super(null);
		this.value = value;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		dirs.returnValue(this.value.writeValue(dirs.valDirs(), host));
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.value.toString();
	}

}
