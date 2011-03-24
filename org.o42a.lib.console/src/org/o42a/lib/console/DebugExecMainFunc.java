/*
    Console Module
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.lib.console.DebuggableMainFunc.DEBUGGABLE_MAIN;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class DebugExecMainFunc extends Func {

	public static final DebugExecMain DEBUG_EXEC_MAIN = new DebugExecMain();

	private DebugExecMainFunc(FuncCaller<DebugExecMainFunc> caller) {
		super(caller);
	}

	public Int32op call(
			Code code,
			DebuggableMainFunc main,
			Int32op argc,
			AnyOp argv) {
		return invoke(code, DEBUG_EXEC_MAIN.result(), main, argc, argv);
	}

	public static final class DebugExecMain
			extends Signature<DebugExecMainFunc> {

		private Return<Int32op> result;
		private Arg<DebuggableMainFunc> main;
		private Arg<Int32op> argc;
		private Arg<AnyOp> argv;

		private DebugExecMain() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Return<Int32op> result() {
			return this.result;
		}

		public final Arg<DebuggableMainFunc> main() {
			return this.main;
		}

		public final Arg<Int32op> argc() {
			return this.argc;
		}

		public final Arg<AnyOp> argv() {
			return this.argv;
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("console").sub("DebugExecMainF");
		}

		@Override
		public DebugExecMainFunc op(FuncCaller<DebugExecMainFunc> caller) {
			return new DebugExecMainFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnInt32();
			this.main = builder.addFuncPtr("main", DEBUGGABLE_MAIN);
			this.argc = builder.addInt32("argc");
			this.argv = builder.addPtr("argv");
		}

	}

}
