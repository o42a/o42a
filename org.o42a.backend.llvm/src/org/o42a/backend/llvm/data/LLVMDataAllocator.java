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

	private DataLayout int8layout;
	private DataLayout int16layout;
	private DataLayout int32layout;
	private DataLayout int64layout;
	private DataLayout fp32layout;
	private DataLayout fp64layout;
	private DataLayout ptrLayout;
	private DataLayout relPtrLayout;

	LLVMDataAllocator(LLVMModule module) {
		this.module = module;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	@Override
	public DataAllocation<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {

		final long nativePtr = binaryConstant(
				getModulePtr(),
				id.getId(),
				data,
				start,
				end,
				isConstant);

		return new AnyAlloc(getModule(), dataId(id, nativePtr), null);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(Type<S> type) {
		return new TypeAllocation<S>(
				getModule(),
				createType(getModulePtr()),
				createTypeData(getModulePtr()),
				type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			DataAllocation<S> type,
			Global<S, ?> global) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {
			typePtr = createType(getModulePtr());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return new ContainerAllocation.Global<S>(
				getModule(),
				typePtr,
				typeDataPtr,
				global.getId(),
				global.getInstance());
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> enter(
			DataAllocation<?> enclosing,
			SubData<S> data,
			DataAllocation<S> type) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {
			typePtr = createType(getModulePtr());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return new ContainerAllocation.Struct<S>(
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
					data.getInstance().getId().getId(),
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
				(ContainerAllocation.Global<?>)
				global.getPointer().getAllocation();

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
	public DataAllocation<Int8recOp> allocateInt8(
			DataAllocation<?> enclosing,
			Int8rec data,
			DataAllocation<Int8recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 8);
		}
		return new Int8dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int16recOp> allocateInt16(
			DataAllocation<?> enclosing,
			Int16rec data,
			DataAllocation<Int16recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 16);
		}
		return new Int16dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int32recOp> allocateInt32(
			DataAllocation<?> enclosing,
			Int32rec data,
			DataAllocation<Int32recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 32);
		}
		return new Int32dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int64recOp> allocateInt64(
			DataAllocation<?> enclosing,
			Int64rec data,
			DataAllocation<Int64recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 64);
		}
		return new Int64dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Fp32recOp> allocateFp32(
			DataAllocation<?> enclosing,
			Fp32rec data,
			DataAllocation<Fp32recOp> type) {
		if (allocate(enclosing)) {
			allocateFp32(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp32dataAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Fp64recOp> allocateFp64(
			DataAllocation<?> enclosing,
			Fp64rec data,
			DataAllocation<Fp64recOp> type) {
		if (allocate(enclosing)) {
			allocateFp64(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp64dataAlloc(container(enclosing));
	}

	@Override
	public <F extends Func<F>> DataAllocation<FuncOp<F>> allocateFuncPtr(
			DataAllocation<?> enclosing,
			FuncRec<F> data,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature) {
		if (allocate(enclosing)) {
			allocateFuncPtr(
					typeDataPtr(enclosing),
					getModule().nativePtr(signature));
		}
		return new FuncPtrAlloc<F>(container(enclosing), signature);
	}

	@Override
	public DataAllocation<AnyOp> allocatePtr(
			DataAllocation<?> enclosing,
			AnyPtrRec data,
			DataAllocation<AnyOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<DataOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataRec data,
			DataAllocation<DataOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyDataAlloc(container(enclosing));
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> allocatePtr(
			DataAllocation<?> enclosing,
			StructRec<S> data,
			DataAllocation<S> type,
			DataAllocation<S> struct) {

		final ContainerAllocation<S> llvmStruct =
				(ContainerAllocation<S>) struct;

		if (allocate(enclosing)) {
			allocateStructPtr(
					typeDataPtr(enclosing),
					llvmStruct.getTypePtr());
		}

		return new SimpleDataAllocation.StructPtr<S>(
				container(enclosing),
				llvmStruct.getType());
	}

	@Override
	public DataAllocation<RelRecOp> allocateRelPtr(
			DataAllocation<?> enclosing,
			RelRec data,
			DataAllocation<RelRecOp> type) {
		if (allocate(enclosing)) {
			allocateRelPtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new RelDataAlloc(container(enclosing));
	}

	@Override
	public String toString() {
		return "LLVM data allocator";
	}

	final DataLayout int8layout() {
		if (this.int8layout != null) {
			return this.int8layout;
		}
		return this.int8layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 8));
	}

	final DataLayout int16layout() {
		if (this.int16layout != null) {
			return this.int16layout;
		}
		return this.int16layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 16));
	}

	final DataLayout int32layout() {
		if (this.int32layout != null) {
			return this.int32layout;
		}
		return this.int32layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 32));
	}

	final DataLayout int64layout() {
		if (this.int64layout != null) {
			return this.int64layout;
		}
		return this.int64layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 64));
	}

	final DataLayout fp32layout() {
		if (this.fp32layout != null) {
			return this.fp32layout;
		}
		return this.fp32layout = new DataLayout(fp32layout(getModulePtr()));
	}

	final DataLayout fp64layout() {
		if (this.fp64layout != null) {
			return this.fp64layout;
		}
		return this.fp64layout = new DataLayout(fp64layout(getModulePtr()));
	}

	final DataLayout ptrLayout() {
		if (this.ptrLayout != null) {
			return this.ptrLayout;
		}
		return this.ptrLayout = new DataLayout(ptrLayout(getModulePtr()));
	}

	final DataLayout relPtrLayout() {
		if (this.relPtrLayout != null) {
			return this.relPtrLayout;
		}
		return this.relPtrLayout = new DataLayout(relPtrLayout(getModulePtr()));
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
			int end,
			boolean isConstant);

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

	private static native void allocateInt(
			long modulePtr,
			long enclosingPtr,
			byte numBits);

	private static native void allocateFp32(long modulePtr, long enclosingPtr);

	private static native void allocateFp64(long modulePtr, long enclosingPtr);

	private static native void allocateFuncPtr(
			long enclosingPtr,
			long functTypePtr);

	private static native void allocatePtr(long modulePtr, long enclosingPtr);

	private static native void allocateStructPtr(
			long enclosingPtr,
			long typePtr);

	private static native void allocateRelPtr(
			long modulePtr,
			long enclosingPtr);

	private static native int intLayout(long modulePtr, byte numBits);

	private static native int fp32layout(long modulePtr);

	private static native int fp64layout(long modulePtr);

	private static native int ptrLayout(long modulePtr);

	private static native int relPtrLayout(long modulePtr);

	private static native int structLayout(long modulePtr, long typePtr);

}
