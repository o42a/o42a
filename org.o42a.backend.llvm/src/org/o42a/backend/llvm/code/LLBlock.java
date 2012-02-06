/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.backend.llvm.code.op.LLOp;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;


public abstract class LLBlock extends LLCode implements BlockWriter {

	private LLCodePos.Head head;
	private LLCodePos.Tail tail;
	private long firstBlockPtr;
	private long blockPtr;
	private int blockIdx;

	LLBlock(
			LLVMModule module,
			LLFunction<?> function,
			Block code,
			CodeId id) {
		super(module, function, code, id);
	}

	public final Block block() {
		return (Block) code();
	}

	@Override
	public boolean created() {
		return this.firstBlockPtr != 0L;
	}

	@Override
	public boolean exists() {
		return this.blockPtr != 0L;
	}

	@Override
	public LLCodePos.Head head() {
		return this.head;
	}

	@Override
	public LLCodePos.Tail tail() {
		if (this.tail != null) {
			assert this.tail.getBlockPtr() == getBlockPtr() :
				"Wrong tail position";
			return this.tail;
		}

		final long nextPtr;
		final long prevPtr = getBlockPtr();

		if (prevPtr != 0L) {
			endBlock();
			nextPtr = nextPtr();
			instr(go(prevPtr, nextInstr(), nextPtr));
		} else {
			nextPtr = nextPtr();
		}

		return this.tail;
	}

	@Override
	public final long nextPtr() {

		final long prevPtr = getBlockPtr();

		if (prevPtr != 0) {
			this.tail = null;
			return prevPtr;// block isn't fulfilled yet
		}

		return setNextPtr(createNextBlock());
	}

	@Override
	public final long nextInstr() {
		return 0L;
	}

	@Override
	public LLAllocation allocationBlock(AllocationCode code, CodeId id) {
		return new LLAllocation(this, code, id);
	}

	@Override
	public void go(CodePos pos) {
		instr(go(nextPtr(), nextInstr(), blockPtr(pos)));
		endBlock();
	}

	@Override
	public void go(BoolOp condition, CodePos truePos, CodePos falsePos) {

		final long blockPtr = nextPtr();
		final long truePtr;
		final long falsePtr;

		endBlock();

		final LLCodePos llvmTrue = llvm(truePos);
		final LLCodePos llvmFalse = llvm(falsePos);

		if (llvmTrue == null || llvmTrue.tailOf(this)) {
			truePtr = nextPtr();
		} else {
			truePtr = llvmTrue.getBlockPtr();
		}
		if (llvmFalse == null || llvmFalse.tailOf(this)) {
			falsePtr = nextPtr();
		} else {
			falsePtr = llvmFalse.getBlockPtr();
		}

		instr(choose(
				blockPtr,
				nextInstr(),
				nativePtr(condition),
				truePtr,
				falsePtr));
	}

	@Override
	public void returnVoid() {
		getFunction().getCallback().beforeReturn(block());
		instr(returnVoid(nextPtr(), nextInstr()));
	}

	public void returnValue(LLOp<?> result) {
		getFunction().getCallback().beforeReturn(block());
		instr(returnValue(nextPtr(), nextInstr(),result.getNativePtr()));
	}

	protected final void init() {
		this.head = new LLCodePos.Head(this);
		this.tail = new LLCodePos.Tail(this);
	}

	protected abstract long createFirtsBlock();

	final long getFirstBlockPtr() {
		if (created()) {
			return this.firstBlockPtr;
		}
		return this.firstBlockPtr = this.blockPtr = createFirtsBlock();
	}

	final long getBlockPtr() {
		if (created()) {
			return this.blockPtr;
		}
		return getFirstBlockPtr();
	}

	private long createNextBlock() {
		return createBlock(getFunction(), getId().anonymous(++this.blockIdx));
	}

	private long setNextPtr(final long nextPtr) {
		this.tail = new LLCodePos.Tail(this, nextPtr);
		return this.blockPtr = nextPtr;
	}

	private final void endBlock() {
		this.blockPtr = 0;
		this.tail = null;
	}

}
