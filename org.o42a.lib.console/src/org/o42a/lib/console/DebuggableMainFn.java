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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class DebuggableMainFn extends Fn<DebuggableMainFn> {

	public static final
	ExtSignature<Int32op, DebuggableMainFn> DEBUGGABLE_MAIN =
			customSignature(CONSOLE_ID.sub("DebuggableMainF"), 2)
			.addInt32("argc")
			.addPtr("argv")
			.returnInt32(c -> new DebuggableMainFn(c));

	private DebuggableMainFn(FuncCaller<DebuggableMainFn> caller) {
		super(caller);
	}

	public Int32op call(Code code, Int32op argc, AnyOp argv) {
		return invoke(null, code, DEBUGGABLE_MAIN.result(), argc, argv);
	}

}
