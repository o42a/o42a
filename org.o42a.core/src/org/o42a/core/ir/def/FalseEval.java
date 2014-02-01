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
package org.o42a.core.ir.def;

import org.o42a.core.ir.op.HostOp;
import org.o42a.util.fn.Cancelable;


final class FalseEval extends InlineEval {

	static final FalseEval INSTANCE = new FalseEval();

	private FalseEval() {
		super(null);
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		dirs.code().go(dirs.falseDir());
	}

	@Override
	public String toString() {
		return "FALSE";
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
