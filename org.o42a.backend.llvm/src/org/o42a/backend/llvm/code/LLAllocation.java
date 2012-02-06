/*
    Compiler LLVM Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.CodeWriter;


final class LLAllocation extends LLInset implements AllocationWriter {

	private long stackPtr;
	private long firstInstr;

	LLAllocation(
			LLCode enclosing,
			LLInset prevInset,
			AllocationCode code,
			CodeId id) {
		super(enclosing, prevInset, code, id);
	}

	@Override
	public void dispose(CodeWriter writer) {
		if (!exists()) {
			// No allocations done. Nothing to dispose.
			return;
		}

		final LLCode llvm = llvm(writer);

		llvm.instr(stackRestore(llvm.nextPtr(), llvm.nextInstr(), stackPtr()));
	}

	@Override
	public long instr(long instr) {
		if (this.firstInstr == 0L) {
			this.firstInstr = instr;
		}
		return super.instr(instr);
	}

	private final AllocationCode allocation() {
		return (AllocationCode) code();
	}

	private final long stackPtr() {
		assert allocation().isDisposable() :
			this + " is not disposable, so stack is not saved";
		if (this.stackPtr != 0L) {
			return this.stackPtr;
		}
		return this.stackPtr = instr(stackSave(
				nextPtr(),
				this.firstInstr != 0L ? this.firstInstr : nextInstr()));
	}

}
