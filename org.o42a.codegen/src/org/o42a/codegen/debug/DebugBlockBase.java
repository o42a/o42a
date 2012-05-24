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

import static org.o42a.codegen.debug.DebugDoFunc.DEBUG_DO;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;
import static org.o42a.codegen.debug.TaskBlock.TASK_DISPOSAL;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpBlockBase;


public abstract class DebugBlockBase extends OpBlockBase {

	public DebugBlockBase(Code enclosing, CodeId name) {
		super(enclosing, name);
	}

	public DebugBlockBase(Generator generator, CodeId id) {
		super(generator, id);
	}

	public final TaskBlock begin(CodeId id, String comment) {

		final Block block = block();

		if (!isDebug()) {
			return new TaskBlock(block, block);
		}

		final Allocator code = block.allocator(id != null ? id : id("debug"));

		code.allocation().addDisposal(TASK_DISPOSAL);

		final DebugStackFrameOp stackFrame = code.allocation().allocate(
				code.id("task_stack_frame"),
				DEBUG_STACK_FRAME_TYPE);

		stackFrame.comment(code).store(code, code.nullPtr());
		stackFrame.file(code).store(code, code.nullPtr());
		stackFrame.line(code).store(code, code.int32(0));

		getGenerator().externalFunction()
		.link("o42a_dbg_do", DEBUG_DO)
		.op(null, code)
		.call(code, stackFrame, comment);

		return new TaskBlock(block, code);
	}

	private final Block block() {
		return (Block) this;
	}

}
