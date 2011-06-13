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

import static org.o42a.backend.llvm.data.LLVMId.dataId;
import static org.o42a.backend.llvm.data.LLVMId.nullId;

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public abstract class ContainerAllocation<O extends StructOp>
		extends LLVMDataAllocation<O> {

	private final Type<O> type;
	private final long typePtr;
	private long uniqueTypePtr;
	private long nativePtr;
	private int nextIndex;

	ContainerAllocation(
			LLVMModule module,
			long typePtr,
			long typeDataPtr,
			ContainerAllocation<?> enclosing,
			Type<O> type) {
		super(module, enclosing);
		this.typePtr = typePtr;
		this.nativePtr = typeDataPtr;
		this.type = type;
		if (typeDataPtr != 0L) {
			this.uniqueTypePtr = 0L;
		} else {

			final ContainerAllocation<?> typeAllocation =
				(ContainerAllocation<?>) type.pointer(module.getGenerator())
				.getAllocation();

			this.uniqueTypePtr = typeAllocation.getUniqueTypePtr();
		}

		assert typePtr != 0L :
			"Type not created";
	}

	public final long getTypePtr() {
		return this.typePtr;
	}

	public final long getUniqueTypePtr() {
		return this.uniqueTypePtr;
	}

	public final Type<O> getType() {
		return this.type;
	}

	@Override
	public final DataLayout getLayout() {
		return getModule().dataAllocator().structLayout(this);
	}

	@Override
	public O op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final LLVMCode code = (LLVMCode) writer;

		return getType().op(new LLVMStruct(
				id,
				allocClass,
				getType(),
				code.nextPtr(),
				llvmId().expression(code.getModule())));
	}

	final boolean isTypeAllocated() {
		return this.uniqueTypePtr != 0L;
	}

	final long getTypeDataPtr() {
		assert !isTypeAllocated() :
			"Type already allocated";
		return this.nativePtr;
	}

	final long getNativePtr() {
		assert isTypeAllocated() :
			"Type not allocated yet";
		return this.nativePtr;
	}

	final void setUniqueTypePtr(long uniqueTypePtr) {
		assert !isTypeAllocated() :
			"Type already allocated";
		this.uniqueTypePtr = uniqueTypePtr;
	}

	final void setNativePtr(long nativePtr) {
		assert isTypeAllocated() :
			"Type not allocated yet";
		this.nativePtr = nativePtr;
	}

	final LLVMId nextId() {
		return llvmId().addIndex(this.nextIndex++);
	}

	static final class Null<O extends StructOp> extends ContainerAllocation<O> {

		private final LLVMId llvmId;

		Null(LLVMModule module, long nativePtr, Type<O> type) {
			super(
					module,
					((ContainerAllocation<?>)
							type.pointer(module.getGenerator())
							.getAllocation()).getTypePtr(),
					nativePtr,
					null,
					type);
			this.llvmId = nullId(nativePtr, false);
		}

		@Override
		public LLVMId llvmId() {
			return this.llvmId;
		}

	}

	static final class Global<O extends StructOp>
			extends ContainerAllocation<O> {

		private final LLVMId llvmId;

		Global(
				LLVMModule module,
				long typePtr,
				long typeDataPtr,
				CodeId id,
				Type<O> type) {
			super(module, typePtr, typeDataPtr, null, type);
			this.llvmId = dataId(id, this);
		}

		@Override
		public LLVMId llvmId() {
			return this.llvmId;
		}

	}

	static final class Struct<O extends StructOp>
			extends ContainerAllocation<O> {

		private final LLVMId llvmId;

		Struct(
				long typePtr,
				long nativePtr,
				ContainerAllocation<?> enclosing,
				Type<O> type) {
			super(
					enclosing.getModule(),
					typePtr,
					nativePtr,
					enclosing, type);
			this.llvmId = enclosing.nextId();
		}

		@Override
		public LLVMId llvmId() {
			return this.llvmId;
		}

	}

}
