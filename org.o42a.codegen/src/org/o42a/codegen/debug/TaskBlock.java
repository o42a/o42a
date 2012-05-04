/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.DebugCodeBase.binaryMessage;
import static org.o42a.codegen.debug.DebugCodeBase.printWoPrefixFunc;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.codegen.code.op.StructRecOp;


public final class TaskBlock {

	static final Disposal TASK_DISPOSAL = new TaskDisposal();

	private final Block enclosing;
	private final Block block;

	TaskBlock(Block enclosing, Block block) {
		this.enclosing = enclosing;
		this.block = block;
	}

	public final Block code() {
		return this.block;
	}

	public final Block end() {
		if (!code().isDebug()) {
			return this.block;
		}

		if (this.block.exists()) {
			this.block.go(this.enclosing.tail());
		}

		return this.enclosing;
	}

	private static final class TaskDisposal implements Disposal {

		@Override
		public void dispose(Code code) {

			final Function<?> function = code.getFunction();
			final DebugEnvOp debugEnv = function.debugEnv(code);

			final StructRecOp<DebugStackFrameOp> envStackFrame =
					debugEnv.stackFrame(code);
			final DebugStackFrameOp prevStackFrame =
					envStackFrame.load(null, code).prev(code).load(null, code);
			final AnyOp comment = prevStackFrame.comment(code).load(null, code);

			prevStackFrame.comment(code).store(code, code.nullPtr());
			envStackFrame.store(code, prevStackFrame);

			final Int8recOp indent = debugEnv.indent(code);

			indent.store(
					code,
					indent.load(null, code)
					.sub(null, code, code.int8((byte) 1)));

			final DebugPrintFunc printFunc =
					printWoPrefixFunc(code.getGenerator()).op(null, code);

			code.debug("))) /* ", false);
			printFunc.call(code, comment);
			printFunc.call(
					code,
					binaryMessage(code.getGenerator(), " */\n")
					.op(null, code));
		}

	}

}
