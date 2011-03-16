/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.data;

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.backend.llvm.code.op.LLVMRelOp;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;


final class LLVMRelAllocation implements RelAllocation {

	private final LLVMId id;
	private final LLVMId relativeTo;

	LLVMRelAllocation(LLVMId id, LLVMId relativeTo) {
		this.id = id;
		this.relativeTo = relativeTo;
	}

	@Override
	public void write(
			DataAllocation<RecOp<RelOp>> allocation,
			DataWriter writer) {

		final LLVMDataWriter llvmWriter = (LLVMDataWriter) writer;

		llvmWriter.writeRelPtr(this.id.relativeExpression(
				llvmWriter.getModule(),
				this.relativeTo));
	}

	@Override
	public LLVMRelOp op(CodeWriter writer) {

		final LLVMCode code = (LLVMCode) writer;

		return new LLVMRelOp(
				code.nextPtr(),
				this.id.relativeExpression(
						code.getModule(),
						this.relativeTo));
	}

}
