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

import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.RelPtr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;


public abstract class LLVMDataAllocation<P extends PtrOp<P>>
		implements DataAllocation<P>, LLVMAllocation {

	private final LLVMModule module;
	private final ContainerAllocation<?> enclosing;

	public LLVMDataAllocation(
			LLVMModule module,
			ContainerAllocation<?> enclosing) {
		this.module = module;
		this.enclosing = enclosing;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	public final ContainerAllocation<?> getEnclosing() {
		return this.enclosing;
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return new AnyAlloc(getModule(), llvmId().toAny(), getEnclosing());
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return new DataAlloc(getModule(), llvmId().toAny(), getEnclosing());
	}

	@Override
	public <R extends RecOp<R, P>> void write(
			DataWriter writer,
			DataAllocation<R> destination) {
		llvmId().write(writer);
	}

	@Override
	public RelAllocation relativeTo(
			RelPtr pointer,
			DataAllocation<?> allocation) {
		return llvmId().relativeTo(allocation);
	}

	@Override
	public String toString() {

		final LLVMId llvmId = llvmId();

		return llvmId != null ? llvmId.toString() : getClass().getSimpleName();
	}

}
