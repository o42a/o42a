/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.DebugDoFunc.DEBUG_DO;
import static org.o42a.codegen.debug.DebugDoneFunc.DEBUG_DONE;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.OpBlockBase;
import org.o42a.util.string.ID;


public abstract class DebugBlockBase extends OpBlockBase {

	private static final ID DEBUG_BLOCK_ID = ID.id("__debug__");
	private static final ID STACK_FRAME_ID = ID.id("__stack_frame__");
	private static final AllocatableStackFrame ALLOCATABLE_STACK_FRAME =
			new AllocatableStackFrame();

	public DebugBlockBase(Code enclosing, ID name) {
		super(enclosing, name);
	}

	public DebugBlockBase(Generator generator, ID id) {
		super(generator, id);
	}

	public final TaskBlock begin(ID id, String comment) {

		final Block block = block();

		if (getGenerator().getDebug().isDebugBlocksOmitted()) {
			return new TaskBlock(block, block);
		}

		final Allocator code =
				block.allocator(id != null ? id : DEBUG_BLOCK_ID);
		final DebugStackFrameOp stackFrame = code.allocation().allocate(
				STACK_FRAME_ID,
				ALLOCATABLE_STACK_FRAME).get();

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

	private static final class AllocatableStackFrame
			implements Allocatable<DebugStackFrameOp> {

		@Override
		public boolean isImmedite() {
			return true;
		}

		@Override
		public int getPriority() {
			return DEBUG_ALLOC_PRIORITY;
		}

		@Override
		public DebugStackFrameOp allocate(
				AllocationCode<DebugStackFrameOp> code) {
			return code.allocate(DEBUG_STACK_FRAME_TYPE);
		}

		@Override
		public void initialize(
				AllocationCode<DebugStackFrameOp> code) {
		}

		@Override
		public void dispose(
				Code code,
				Allocated<DebugStackFrameOp> allocated) {
			code.getGenerator()
			.externalFunction()
			.link("o42a_dbg_done", DEBUG_DONE)
			.op(null, code)
			.call(code);
		}

	}

}
