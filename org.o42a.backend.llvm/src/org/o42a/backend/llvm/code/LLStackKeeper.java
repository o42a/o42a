/*
    Compiler LLVM Back-end
    Copyright (C) 2014 Ruslan Lopatin

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

import static org.o42a.backend.llvm.code.LLCode.*;
import static org.o42a.codegen.code.op.Op.PHI_ID;

import java.util.Arrays;
import java.util.IdentityHashMap;

import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.backend.AllocatorWriter;


final class LLStackKeeper implements AllocatorWriter {

	private final IdentityHashMap<Allocator, StackPtrs> stackPtrs =
			new IdentityHashMap<>(1);

	@Override
	public void allocate(Code code, CodePos target) {
		ptrs(target).allocate(code);
	}

	@Override
	public void combine(Code code, Code originalCode) {

		final StackPtrs ptrs = this.stackPtrs.get(originalCode.getAllocator());

		assert ptrs != null :
			"Can not combine allocations that never made";

		ptrs.combine(code, originalCode);
	}

	@Override
	public void dispose(Code code, Code originalCode) {

		final StackPtrs ptrs = this.stackPtrs.get(originalCode.getAllocator());

		if (ptrs != null) {
			ptrs.dispose(code);
		}
	}

	private StackPtrs ptrs(CodePos target) {

		final Allocator allocator = target.code().getAllocator();
		final StackPtrs existing = this.stackPtrs.get(allocator);

		if (existing != null) {
			return existing;
		}

		final StackPtrs ptrs = new StackPtrs();

		this.stackPtrs.put(allocator, ptrs);

		return ptrs;
	}

	private StackPtrs find(Allocator allocator) {

		Allocator alloc = allocator;

		while (alloc != null) {

			final StackPtrs found = this.stackPtrs.get(alloc);

			if (found != null) {
				return found;
			}

			alloc = alloc.getEnclosingAllocator();
		}

		return null;
	}

	private final class StackPtrs {

		private long[] ptrs;
		private long blockPtr;
		private long stackPtr;

		void allocate(Code code) {

			final LLCode llvm = llvm(code);
			final long nextPtr = llvm.nextPtr();
			final long stackPtr =
					llvm.instr(stackSave(nextPtr, llvm.nextInstr()));

			if (this.ptrs == null) {
				this.ptrs = new long[] {nextPtr, stackPtr};
			} else {

				final int len = this.ptrs.length;

				this.ptrs = Arrays.copyOf(this.ptrs, len + 2);
				this.ptrs[len] = nextPtr;
				this.ptrs[len + 1] = stackPtr;
			}
		}

		void combine(Code code, Code originalCode) {

			final StackPtrs enclosingPtrs =
					find(originalCode.getAllocator().getEnclosingAllocator());
			final LLCode llvm = llvm(code);

			if (enclosingPtrs == null) {
				combineThis(llvm);
			} else {
				combineNested(llvm, enclosingPtrs);
			}
		}

		void dispose(Code code) {
			assert this.stackPtr != 0 :
				"Stack allocations not combined";

			final LLCode llvm = llvm(code);

			llvm.instr(stackRestore(
					llvm.nextPtr(),
					llvm.nextInstr(),
					this.stackPtr));
		}

		private void combineThis(LLCode llvm) {
			if (this.ptrs == null) {
				this.blockPtr = llvm.nextPtr();
				this.stackPtr =
						llvm.instr(stackSave(this.blockPtr, llvm.nextInstr()));
			} else if (this.ptrs.length == 2) {
				this.blockPtr = this.ptrs[0];
				this.stackPtr = this.ptrs[1];
			} else {

				final NativeBuffer ids = llvm.getModule().ids();

				this.blockPtr = llvm.nextPtr();
				this.stackPtr = llvm.instr(phiN(
						this.blockPtr,
						llvm.nextInstr(),
						ids.write(PHI_ID),
						ids.length(),
						this.ptrs));
			}
		}

		private void combineNested(LLCode llvm, StackPtrs enclosingPtrs) {
			assert enclosingPtrs.stackPtr != 0 :
				"Enclosing allocations not combined yet";

			if (this.ptrs == null) {
				this.stackPtr = enclosingPtrs.stackPtr;
			} else {

				final int len = this.ptrs.length;
				final long[] ptrs = Arrays.copyOf(this.ptrs, len + 2);

				ptrs[len] = enclosingPtrs.blockPtr;
				ptrs[len + 1] = enclosingPtrs.stackPtr;

				final NativeBuffer ids = llvm.getModule().ids();

				this.blockPtr = llvm.nextPtr();
				this.stackPtr = llvm.instr(phiN(
						this.blockPtr,
						llvm.nextInstr(),
						ids.write(PHI_ID),
						ids.length(),
						ptrs));
			}
		}

	}

}
