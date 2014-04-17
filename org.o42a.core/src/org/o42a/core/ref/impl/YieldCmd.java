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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePtr;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.ResumeCallback;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.util.string.ID;


final class YieldCmd implements Cmd {

	private static final ID YIELD_ID = ID.rawId("yield");
	private static final ID PREPARE_RESUME_ID = ID.rawId("prepare_resume");

	private final Eval eval;

	YieldCmd(Eval eval) {
		this.eval = eval;
	}

	@Override
	public void write(Control control) {

		final DefDirs controlDirs = control.defDirs();
		final Block yield = controlDirs.addBlock(YIELD_ID);
		final DefDirs dirs = controlDirs.setReturnDir(yield.head());

		this.eval.write(dirs, control.host());

		dirs.done();

		yield(control, controlDirs, yield);
	}

	@Override
	public String toString() {
		if (this.eval == null) {
			return super.toString();
		}
		return "<<" + this.eval;
	}

	private void yield(Control control, DefDirs controlDirs, Block code) {
		if (!code.exists()) {
			return;
		}

		code.dump("Yield: ", controlDirs.result());

		final Code prepareResume = code.inset(PREPARE_RESUME_ID);
		final ObjectOp host = control.host();

		control.setResumeCallback(new ResumeCallback() {
			@Override
			public void resumedAt(CodePtr resumePtr) {
				host.objectData(prepareResume)
				.ptr()
				.resumeFrom(prepareResume)
				.store(
						prepareResume,
						resumePtr.toAny().op(null, prepareResume));
			}
		});

		code.go(controlDirs.returnDir());
	}

}
