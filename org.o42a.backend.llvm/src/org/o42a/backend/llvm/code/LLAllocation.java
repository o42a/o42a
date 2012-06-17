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

import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.llvm.code.rec.AnyRecLLOp;
import org.o42a.backend.llvm.code.rec.StructRecLLOp;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.string.ID;


final class LLAllocation extends LLInset implements AllocationWriter {

	private long stackPtr;
	private long firstInstr;

	LLAllocation(LLCode enclosing, Code code) {
		super(enclosing, code);
	}

	@Override
	public <S extends StructOp<S>> S allocateStruct(
			ID id,
			DataAllocation<S> allocation) {

		final ContainerLLDAlloc<S> type =
				(ContainerLLDAlloc<S>) allocation;
		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return type.getType().op(new LLStruct<S>(
				id,
				AUTO_ALLOC_CLASS,
				type,
				nextPtr,
				instr(allocateStruct(
						nextPtr,
						nextInstr(),
						ids.write(id),
						ids.length(),
						type.getTypePtr()))));
	}

	@Override
	public AnyRecLLOp allocatePtr(ID id) {

		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return new AnyRecLLOp(
				id,
				AUTO_ALLOC_CLASS,
				nextPtr,
				instr(allocatePtr(
						nextPtr,
						nextInstr(),
						ids.write(id),
						ids.length())));
	}

	@Override
	public <S extends StructOp<S>> StructRecLLOp<S> allocatePtr(
			ID id,
			DataAllocation<S> allocation) {

		final ContainerLLDAlloc<S> alloc =
				(ContainerLLDAlloc<S>) allocation;
		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return new StructRecLLOp<S>(
				id,
				AUTO_ALLOC_CLASS,
				alloc.getType(),
				nextPtr,
				instr(allocateStructPtr(
						nextPtr,
						nextInstr(),
						ids.write(id),
						ids.length(),
						alloc.getTypePtr())));
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
	protected void addInstr(long instr) {
		if (this.firstInstr == 0L) {
			this.firstInstr = instr;
		}
		super.addInstr(instr);
	}

	private final long stackPtr() {
		if (this.stackPtr != 0L) {
			return this.stackPtr;
		}
		return this.stackPtr = instr(stackSave(
				nextPtr(),
				this.firstInstr != 0L ? this.firstInstr : nextInstr()));
	}

}
