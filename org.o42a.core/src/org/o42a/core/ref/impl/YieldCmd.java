/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ref.impl;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;


final class YieldCmd implements Cmd {

	private final Eval eval;

	YieldCmd(Eval eval) {
		this.eval = eval;
	}

	@Override
	public void write(Control control) {

		final DefDirs dirs = control.defDirs();

		this.eval.write(dirs, control.host());

		final Block code = dirs.done().code();
		final Block afterYield = code.addBlock("after_yield");

		control.resumeFrom(afterYield);
		control.host()
		.objectData(code)
		.ptr()
		.resumeFrom(code)
		.store(code, afterYield.ptr().toAny().op(afterYield.getId(), code));

		afterYield.go(code.tail());
	}

	@Override
	public String toString() {
		if (this.eval == null) {
			return super.toString();
		}
		return "<<" + this.eval;
	}

}
