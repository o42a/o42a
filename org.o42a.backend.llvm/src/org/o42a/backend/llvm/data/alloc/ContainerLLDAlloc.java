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
package org.o42a.backend.llvm.data.alloc;


import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.Type;


public abstract class ContainerLLDAlloc<S extends StructOp<S>>
		extends LLDAlloc<S> {

	private final Type<S> type;
	private final long typePtr;
	private long nativePtr;
	private int nextIndex;
	private boolean typeAllocated;

	public ContainerLLDAlloc(
			LLVMModule module,
			long typePtr,
			long typeDataPtr,
			ContainerLLDAlloc<?> enclosing,
			Type<S> type) {
		super(module, enclosing);
		this.typePtr = typePtr;
		this.nativePtr = typeDataPtr;
		this.type = type;
		if (typeDataPtr != 0L) {
			this.typeAllocated = false;
		} else {

			final ContainerLLDAlloc<?> typeAllocation =
					(ContainerLLDAlloc<?>) type.pointer(module.getGenerator())
					.getAllocation();

			this.typeAllocated = typeAllocation.isTypeAllocated();
		}

		assert typePtr != 0L :
			"Type not created";
	}

	public final long getTypePtr() {
		return this.typePtr;
	}

	public final boolean isTypeAllocated() {
		return this.typeAllocated;
	}

	public final void typeAllocated() {
		assert !isTypeAllocated() :
			"Type already allocated";
		this.typeAllocated = true;
	}

	public final long getNativePtr() {
		assert isTypeAllocated() :
			"Type not allocated yet";
		return this.nativePtr;
	}

	public final void setNativePtr(long nativePtr) {
		assert isTypeAllocated() :
			"Type not allocated yet";
		this.nativePtr = nativePtr;
	}

	public final long getTypeDataPtr() {
		assert !isTypeAllocated() :
			"Type already allocated";
		return this.nativePtr;
	}

	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public final DataLayout getLayout() {
		return getModule().dataAllocator().structLayout(this);
	}

	@Override
	public S op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final LLCode code = (LLCode) writer;

		return getType().op(new LLStruct<S>(
				id,
				allocClass,
				getType(),
				code.nextPtr(),
				llvmId().expression(code.getModule())));
	}

	final LLVMId nextId() {
		return llvmId().addIndex(this.nextIndex++);
	}

}
