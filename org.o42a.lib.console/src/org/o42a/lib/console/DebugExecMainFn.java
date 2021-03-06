/*
    Console Module
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.lib.console;

import static org.o42a.lib.console.ConsoleModule.CONSOLE_ID;
import static org.o42a.lib.console.DebuggableMainFn.DEBUGGABLE_MAIN;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class DebugExecMainFn extends Fn<DebugExecMainFn> {

	public static final
	ExtSignature<Int32op, DebugExecMainFn> DEBUG_EXEC_MAIN =
			customSignature(CONSOLE_ID.sub("DebugExecMainF"), 3)
			.addFuncPtr("main", DEBUGGABLE_MAIN)
			.addInt32("argc")
			.addPtr("argv")
			.returnInt32(c -> new DebugExecMainFn(c));

	private DebugExecMainFn(FuncCaller<DebugExecMainFn> caller) {
		super(caller);
	}

	public Int32op call(
			Code code,
			DebuggableMainFn main,
			Int32op argc,
			AnyOp argv) {
		return invoke(null, code, DEBUG_EXEC_MAIN.result(), main, argc, argv);
	}

}
