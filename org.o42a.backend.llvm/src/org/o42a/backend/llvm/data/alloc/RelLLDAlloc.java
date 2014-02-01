/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.backend.llvm.data.alloc;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.op.RelLLOp;
import org.o42a.backend.llvm.data.LLVMDataWriter;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;
import org.o42a.util.string.ID;


public final class RelLLDAlloc implements RelAllocation {

	private final LLVMId id;
	private final LLVMId relativeTo;

	public RelLLDAlloc(LLVMId id, LLVMId relativeTo) {
		this.id = id;
		this.relativeTo = relativeTo;
	}

	@Override
	public void write(
			DataWriter writer,
			DataAllocation<RelRecOp> destination) {

		final LLVMDataWriter llvmWriter = (LLVMDataWriter) writer;

		llvmWriter.writeRelPtr(
				destination,
				this.id.relativeExpression(
						llvmWriter.getModule(),
						this.relativeTo));
	}

	@Override
	public RelLLOp op(ID id, CodeWriter writer) {

		final LLCode code = (LLCode) writer;

		return new RelLLOp(
				id,
				code.nextPtr(),
				this.id.relativeExpression(
						code.getModule(),
						this.relativeTo));
	}

}
