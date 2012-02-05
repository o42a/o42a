/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.BoolOp;


final class LLInset extends LLCode {

	private final long blockPtr;
	private LLInset prevInset;
	private long nextInstr;

	LLInset(LLCode enclosing, LLInset prevInset, Code code, CodeId id) {
		super(
				enclosing.getModule(),
				enclosing.getFunction(),
				code,
				id);
		this.prevInset = prevInset;
		this.blockPtr = enclosing.nextPtr();
		assert prevInset == null || prevInset.blockPtr == this.blockPtr :
			this + " belongs to another block than " + prevInset;
	}

	@Override
	public CodePos head() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CodePos tail() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean created() {
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public void done() {
	}

	@Override
	public LLBlock block(Code code, CodeId id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LLAllocation allocationBlock(AllocationCode code, CodeId id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void go(CodePos pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void go(BoolOp condition, CodePos truePos, CodePos falsePos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final long nextPtr() {
		return this.blockPtr;
	}

	@Override
	public long nextInstr() {
		return this.nextInstr;
	}

	@Override
	public long instr(long instr) {
		super.instr(instr);
		if (this.prevInset != null) {
			this.prevInset.nextInstr(instr);
			this.prevInset = null;
		}
		return instr;
	}

	final void nextInstr(long nextInstr) {
		this.nextInstr = nextInstr;
		if (this.prevInset != null) {
			this.prevInset.nextInstr(nextInstr);
		}
	}

}
