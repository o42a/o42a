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

	static ContainerAllocation<?> container(DataAllocation<?> allocation) {
		return (ContainerAllocation<?>) allocation;
	}

	private final LLVMModule module;

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

		return new AnyAlloc(getModule(), dataId(id, nativePtr), null);
	}

	@Override
	public <O extends StructOp> DataAllocation<O> begin(Type<O> type) {
		return new TypeAllocation<O>(
				getModule(),
				createType(getModulePtr()),
				createTypeData(getModulePtr()),
				type);
	}

	@Override
	public <O extends StructOp> DataAllocation<O> begin(
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

		return new ContainerAllocation.Global<O>(
				getModule(),
				typePtr,
				typeDataPtr,
				global.getId(),
				global.getInstance());
	}

	@Override
	public <O extends StructOp> DataAllocation<O> enter(
			DataAllocation<?> enclosing,
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

		return new ContainerAllocation.Struct<O>(
				typePtr,
				typeDataPtr,
				container(enclosing),
				data.getInstance());
	}

	@Override
	public void exit(DataAllocation<?> enclosing, SubData<?> data) {

		final ContainerAllocation<?> allocation =
			container(data.getPointer().getAllocation());

		if (!allocation.isTypeAllocated()) {
			allocation.setUniqueTypePtr(refineType(
					getModulePtr(),
					data.getId().getId(),
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					data.getInstance().isPacked()));
		}
		if (allocate(enclosing)) {
			allocation.setNativePtr(allocateStruct(
					getModulePtr(),
					typeDataPtr(enclosing),
					allocation.getTypePtr()));
		} else {
			allocation.setNativePtr(allocation.getTypePtr());
		}
	}

	@Override
	public void end(Global<?, ?> global) {

		final ContainerAllocation.Global<?> allocation =
			(ContainerAllocation.Global<?>) global.getPointer().getAllocation();

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

		final TypeAllocation<?> allocation =
			(TypeAllocation<?>) type.pointer(
					type.getGenerator()).getAllocation();

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
	public DataAllocation<RecOp<Int32op>> allocateInt32(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int32op>> type) {
		if (allocate(enclosing)) {
			allocateInt32(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Int32dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<RecOp<Int64op>> allocateInt64(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int64op>> type) {
		if (allocate(enclosing)) {
			allocateInt64(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Int64dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<RecOp<Fp64op>> allocateFp64(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Fp64op>> type) {
		if (allocate(enclosing)) {
			allocateFp64(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp64dataAlloc(container(enclosing));
	}

	@Override
	public <F extends Func> DataAllocation<FuncOp<F>> allocateFuncPtr(
			DataAllocation<?> enclosing,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature) {
		if (allocate(enclosing)) {
			allocateCodePtr(
					typeDataPtr(enclosing),
					LLVMCode.nativePtr(signature));
		}
		return new FuncPtrAlloc<F>(container(enclosing), signature);
	}

	@Override
	public DataAllocation<AnyOp> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<AnyOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<DataOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataAllocation<DataOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyDataAlloc(container(enclosing));
	}

	@Override
	public <P extends StructOp> DataAllocation<P> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<P> type,
			DataAllocation<P> struct) {

		final ContainerAllocation<P> llvmStruct =
			(ContainerAllocation<P>) struct;

		if (allocate(enclosing)) {
			allocateStructPtr(
					typeDataPtr(enclosing),
					llvmStruct.getTypePtr());
		}

		return new SimpleDataAllocation.StructPtr<P>(
				container(enclosing),
				llvmStruct.getType());
	}

	@Override
	public DataAllocation<RecOp<RelOp>> allocateRelPtr(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<RelOp>> type) {
		if (allocate(enclosing)) {
			allocateRelPtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new RelDataAlloc(container(enclosing));
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

	private static boolean allocate(DataAllocation<?> enclosing) {
		return !container(enclosing).isTypeAllocated();
	}

	private static long typeDataPtr(DataAllocation<?> enclosing) {
		return container(enclosing).getTypeDataPtr();
	}

	private long getModulePtr() {
		return getModule().getNativePtr();
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
