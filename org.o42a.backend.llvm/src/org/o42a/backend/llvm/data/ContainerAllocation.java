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
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.Type;


public abstract class ContainerAllocation<O extends PtrOp>
		extends LLVMDataAllocation<O> {

	private final Type<O> type;
	private long typePtr;
	private long nativePtr;
	private int nextIndex;

	ContainerAllocation(
			LLVMModule module,
			long typePtr,
			long nativePtr,
			ContainerAllocation<?> enclosing,
			Type<O> type) {
		super(module, enclosing);
		this.typePtr = typePtr;
		this.nativePtr = nativePtr;
		this.type = type;
	}

	public final long getTypePtr() {
		return this.typePtr;
	}

	public final Type<O> getType() {
		return this.type;
	}

	@Override
	public final DataLayout getLayout() {
		return getModule().dataAllocator().structLayout(this);
	}

	@Override
	public O op(CodeWriter writer) {

		final LLVMCode code = (LLVMCode) writer;

		return getType().op(new LLVMStruct(
				getType(),
				code.nextPtr(),
				llvmId().expression(code.getModule())));
	}

	final long getNativePtr() {
		return this.nativePtr;
	}

	final LLVMId nextId() {
		return llvmId().addIndex(this.nextIndex++);
	}

	final void setTypePtr(long typePtr) {
		this.typePtr = typePtr;
	}

	final void setNativePtr(long nativePtr) {
		this.nativePtr = nativePtr;
	}

	static final class Null<O extends PtrOp> extends ContainerAllocation<O> {

		private final LLVMId llvmId;

		Null(LLVMModule module, long nativePtr, Type<O> type) {
			super(
					module,
					((ContainerAllocation<?>)
							type.getPointer().getAllocation()).getTypePtr(),
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

	static final class Global<O extends PtrOp>
			extends ContainerAllocation<O> {

		private final LLVMId llvmId;

		Global(
				LLVMModule module,
				long typePtr,
				long nativePtr,
				ContainerAllocation<?> prev,
				String id,
				Type<O> type) {
			super(module, typePtr, nativePtr, prev, type);
			this.llvmId = dataId(id, this);
		}

		@Override
		public LLVMId llvmId() {
			return this.llvmId;
		}

	}

	static final class Struct<O extends PtrOp> extends ContainerAllocation<O> {

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
