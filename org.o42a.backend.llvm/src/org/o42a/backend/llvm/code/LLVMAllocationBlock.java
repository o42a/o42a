/*
    Compiler LLVM Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.CodeWriter;


final class LLVMAllocationBlock extends LLVMCode implements AllocationWriter {

	private final long stackPtr;

	LLVMAllocationBlock(LLVMCode enclosing, Code code, CodeId id) {
		super(
				enclosing.getModule(),
				enclosing.getFunction(),
				code,
				id);
		init();
		this.stackPtr = stackSave(nextPtr());
	}

	@Override
	public void dispose(CodeWriter writer) {
		stackRestore(nextPtr(writer), this.stackPtr);
	}

	@Override
	public void done() {
	}

	@Override
	protected long createFirtsBlock() {
		return createBlock(getFunction().getFunctionPtr(), getId().getId());
	}

}
