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

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;


public class LLVMDataAllocator implements DataAllocator {

	private final LLVMModule module;
	private ContainerAllocation<?> top;

	LLVMDataAllocator(LLVMModule module) {
		this.module = module;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	@Override
	public DataAllocation<AnyOp> addBinary(
			CodeId id,
			byte[] data,
			int start,
			int end) {

		final long nativePtr =
			binaryConstant(getModulePtr(), id.getId(), data, start, end);

		return new AnyDataAlloc(getModule(), dataId(id, nativePtr), null);
	}

	@Override
	public <O extends PtrOp> DataAllocation<O> begin(Type<O> type) {
		return push(new TypeAllocation<O>(
				getModule(),
				createType(getModulePtr()),
				createTypeData(getModulePtr()),
				this.top,
				type));
	}

	@Override
	public <O extends PtrOp> DataAllocation<O> begin(
			DataAllocation<O> type,
			Global<O, ?> global) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {
			typePtr = createType(getModulePtr());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return push(new ContainerAllocation.Global<O>(
				getModule(),
				typePtr,
				typeDataPtr,
				this.top,
				global.getId(),
				global.getInstance()));
	}

	@Override
	public <O extends PtrOp> DataAllocation<O> enter(
			DataAllocation<O> type,
			SubData<O> data) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {
			typePtr = createType(getModulePtr());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return push(new ContainerAllocation.Struct<O>(
				typePtr,
				typeDataPtr,
				this.top,
				data.getType()));
	}

	@Override
	public void exit(SubData<?> data) {

		final ContainerAllocation<?> allocation = pull();

		if (!allocation.isTypeAllocated()) {
			allocation.setUniqueTypePtr(refineType(
					getModulePtr(),
					data.getId().getId(),
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					data.getType().isPacked()));
		}
		if (allocate()) {
			allocation.setNativePtr(allocateStruct(
					getModulePtr(),
					getTypeDataPtr(),
					allocation.getTypePtr()));
		} else {
			allocation.setNativePtr(allocation.getTypePtr());
		}
	}

	@Override
	public void end(Global<?, ?> global) {

		final ContainerAllocation.Global<?> allocation =
			(ContainerAllocation.Global<?>) pull();

		assert allocation.llvmId().getGlobalId().equals(global.getId()) :
			"Error closing global data " + global;

		if (!allocation.isTypeAllocated()) {
			allocation.setUniqueTypePtr(refineType(
					getModulePtr(),
					global.getId().detail("type").toString(),
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					global.getInstance().isPacked()));
		}
		allocation.setNativePtr(allocateGlobal(
				getModulePtr(),
				global.getId().getId(),
				allocation.getTypePtr(),
				global.isConstant(),
				global.isExported()));
	}

	@Override
	public void end(Type<?> type) {

		final TypeAllocation<?> allocation = (TypeAllocation<?>) pull();

		assert type.pointer(type.getGenerator()).getAllocation() == allocation :
			"Wrong " + type + " allocation: " + allocation
			+ ", but " + type.pointer(type.getGenerator()).getAllocation()
			+ " expected";

		allocation.setUniqueTypePtr(refineType(
				getModulePtr(),
				type.codeId(type.getGenerator()).getId(),
				allocation.getTypePtr(),
				allocation.getTypeDataPtr(),
				type.isPacked()));
		allocation.setNativePtr(allocation.getTypePtr());
	}

	@Override
	public DataAllocation<DataOp<Int32op>> allocateInt32(
			DataAllocation<DataOp<Int32op>> type) {
		if (allocate()) {
			allocateInt32(getModulePtr(), getTypeDataPtr());
		}
		return new Int32dataAlloc(this.top);
	}

	@Override
	public DataAllocation<DataOp<Int64op>> allocateInt64(
			DataAllocation<DataOp<Int64op>> type) {
		if (allocate()) {
			allocateInt64(getModulePtr(), getTypeDataPtr());
		}
		return new Int64dataAlloc(this.top);
	}

	@Override
	public DataAllocation<DataOp<Fp64op>> allocateFp64(
			DataAllocation<DataOp<Fp64op>> type) {
		if (allocate()) {
			allocateFp64(getModulePtr(), getTypeDataPtr());
		}
		return new Fp64dataAlloc(this.top);
	}

	@Override
	public <F extends Func> DataAllocation<CodeOp<F>> allocateCodePtr(
			DataAllocation<CodeOp<F>> type,
			Signature<F> signature) {
		if (allocate()) {
			allocateCodePtr(
					getTypeDataPtr(),
					LLVMCode.nativePtr(signature));
		}
		return new FuncPtrAlloc<F>(this.top, signature);
	}

	@Override
	public DataAllocation<AnyOp> allocatePtr(DataAllocation<AnyOp> type) {
		if (allocate()) {
			allocatePtr(getModulePtr(), getTypeDataPtr());
		}
		return new AnyDataAlloc(this.top);
	}

	@Override
	public <P extends PtrOp> DataAllocation<P> allocatePtr(
			DataAllocation<P> type,
			DataAllocation<P> struct) {

		final ContainerAllocation<P> llvmStruct =
			(ContainerAllocation<P>) struct;

		if (allocate()) {
			allocateStructPtr(
					getTypeDataPtr(),
					llvmStruct.getTypePtr());
		}

		return new SimpleDataAllocation.StructPtr<P>(
				this.top,
				llvmStruct.getType());
	}

	@Override
	public DataAllocation<DataOp<RelOp>> allocateRelPtr(
			DataAllocation<DataOp<RelOp>> type) {
		if (allocate()) {
			allocateRelPtr(getModulePtr(), getTypeDataPtr());
		}
		return new RelDataAlloc(this.top);
	}

	@Override
	public String toString() {
		return "LLVM data allocator";
	}

	final DataLayout int32layout() {
		return new DataLayout(int32layout(getModulePtr()));
	}

	final DataLayout int64layout() {
		return new DataLayout(int64layout(getModulePtr()));
	}

	final DataLayout fp64layout() {
		return new DataLayout(fp64layout(getModulePtr()));
	}

	final DataLayout ptrLayout() {
		return new DataLayout(ptrLayout(getModulePtr()));
	}

	final DataLayout relPtrLayout() {
		return new DataLayout(relPtrLayout(getModulePtr()));
	}

	final DataLayout structLayout(ContainerAllocation<?> type) {
		return new DataLayout(structLayout(getModulePtr(), type.getTypePtr()));
	}

	private boolean allocate() {
		return !this.top.isTypeAllocated();
	}

	private long getTypeDataPtr() {
		return this.top.getTypeDataPtr();
	}

	private long getModulePtr() {
		return getModule().getNativePtr();
	}

	private <O extends PtrOp> ContainerAllocation<O> push(
			ContainerAllocation<O> allocation) {
		this.top = allocation;
		return allocation;
	}

	private ContainerAllocation<?> pull() {

		final ContainerAllocation<?> top = this.top;

		this.top = top.getEnclosing();

		return top;
	}

	private static long typePtr(DataAllocation<?> allocation) {
		return ((ContainerAllocation<?>) allocation).getTypePtr();
	}

	private static native long binaryConstant(
			long modulePtr,
			String id,
			byte[] data,
			int start,
			int end);

	private static native long createType(long modulePtr);

	private static native long createTypeData(long modulePtr);

	private static native long allocateStruct(
			long modulePtr,
			long enclosingPtr,
			long typePtr);

	private static native long allocateGlobal(
			long modulePtr,
			String id,
			long typePtr,
			boolean constant,
			boolean exported);

	private static native long refineType(
			long modulePtr,
			String id,
			long typePtr,
			long typeDataPtr,
			boolean packed);

	private static native void allocateInt32(long modulePtr, long enclosingPtr);

	private static native void allocateInt64(long modulePtr, long enclosingPtr);

	private static native void allocateFp64(long modulePtr, long enclosingPtr);

	private static native void allocateCodePtr(
			long enclosingPtr,
			long functTypePtr);

	private static native void allocatePtr(long modulePtr, long enclosingPtr);

	private static native void allocateStructPtr(
			long enclosingPtr,
			long typePtr);

	private static native void allocateRelPtr(
			long modulePtr,
			long enclosingPtr);

	private static native int int32layout(long modulePtr);

	private static native int int64layout(long modulePtr);

	private static native int fp64layout(long modulePtr);

	private static native int ptrLayout(long modulePtr);

	private static native int relPtrLayout(long modulePtr);

	private static native int structLayout(long modulePtr, long typePtr);

}
