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

import static org.o42a.backend.llvm.id.LLVMId.dataId;

import org.o42a.backend.llvm.data.alloc.*;
import org.o42a.backend.llvm.data.rec.*;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;


public class LLVMDataAllocator implements DataAllocator {

	static ContainerLLDAlloc<?> container(DataAllocation<?> allocation) {
		return (ContainerLLDAlloc<?>) allocation;
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

		final NativeBuffer ids = getModule().ids();
		final long nativePtr = binaryConstant(
				getModulePtr(),
				ids.writeCodeId(id),
				ids.length(),
				data,
				start,
				end,
				isConstant);

		return new AnyLLDAlloc(getModule(), dataId(id, nativePtr), null);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			Type<S> type) {
		return new TypeLLAlloc<S>(
				getModule(),
				createType(getModulePtr()),
				createTypeData(getModulePtr()),
				type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			DataAllocation<S> type, Global<S, ?> global) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {
			typePtr = createType(getModulePtr());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return new GlobalLLDAlloc<S>(
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

		return new StructLLDAlloc<S>(
				typePtr,
				typeDataPtr,
				container(enclosing),
				data.getInstance());
	}

	@Override
	public void exit(DataAllocation<?> enclosing, SubData<?> data) {

		final ContainerLLDAlloc<?> allocation =
				container(data.getPointer().getAllocation());

		if (!allocation.isTypeAllocated()) {

			final NativeBuffer ids = getModule().ids();

			allocation.setUniqueTypePtr(refineType(
					getModulePtr(),
					ids.writeCodeId(data.getInstance().getId()),
					ids.length(),
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

		final GlobalLLDAlloc<?> allocation =
				(GlobalLLDAlloc<?>)
				global.getPointer().getAllocation();

		assert allocation.llvmId().getGlobalId().equals(global.getId()) :
			"Error closing global data " + global;

		final NativeBuffer ids = getModule().ids();

		if (!allocation.isTypeAllocated()) {
			allocation.setUniqueTypePtr(refineType(
					getModulePtr(),
					ids.writeCodeId(global.getId().detail("type")),
					ids.length(),
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					global.getInstance().isPacked()));
		}
		allocation.setNativePtr(allocateGlobal(
				getModulePtr(),
				ids.writeCodeId(global.getId()),
				ids.length(),
				allocation.getTypePtr(),
				global.isConstant(),
				global.isExported()));
	}

	@Override
	public void end(Type<?> type) {

		final TypeLLAlloc<?> allocation =
				(TypeLLAlloc<?>) type.pointer(
						type.getGenerator()).getAllocation();

		assert type.pointer(type.getGenerator()).getAllocation() == allocation :
			"Wrong " + type + " allocation: " + allocation
			+ ", but " + type.pointer(type.getGenerator()).getAllocation()
			+ " expected";

		final NativeBuffer ids = getModule().ids();

		allocation.setUniqueTypePtr(refineType(
				getModulePtr(),
				ids.writeCodeId(type.codeId(type.getGenerator())),
				ids.length(),
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
		return new Int8lldAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int16recOp> allocateInt16(
			DataAllocation<?> enclosing,
			Int16rec data,
			DataAllocation<Int16recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 16);
		}
		return new Int16lldAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int32recOp> allocateInt32(
			DataAllocation<?> enclosing,
			Int32rec data,
			DataAllocation<Int32recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 32);
		}
		return new Int32lldAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Int64recOp> allocateInt64(
			DataAllocation<?> enclosing,
			Int64rec data,
			DataAllocation<Int64recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 64);
		}
		return new Int64lldAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Fp32recOp> allocateFp32(
			DataAllocation<?> enclosing,
			Fp32rec data,
			DataAllocation<Fp32recOp> type) {
		if (allocate(enclosing)) {
			allocateFp32(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp32lldAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<Fp64recOp> allocateFp64(
			DataAllocation<?> enclosing,
			Fp64rec data,
			DataAllocation<Fp64recOp> type) {
		if (allocate(enclosing)) {
			allocateFp64(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp64lldAlloc(container(enclosing));
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
		return new FuncRecLLDAlloc<F>(container(enclosing), signature);
	}

	@Override
	public DataAllocation<AnyRecOp> allocatePtr(
			DataAllocation<?> enclosing,
			AnyRec data,
			DataAllocation<AnyRecOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyRecLLDAlloc(container(enclosing));
	}

	@Override
	public DataAllocation<DataRecOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataRec data,
			DataAllocation<DataRecOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new DataRecLLDAlloc(container(enclosing));
	}


	@Override
	public <S extends StructOp<S>> DataAllocation<StructRecOp<S>> allocatePtr(
			DataAllocation<?> enclosing,
			StructRec<S> data,
			DataAllocation<StructRecOp<S>> type,
			DataAllocation<S> struct) {

		final ContainerLLDAlloc<S> llvmStruct =
				(ContainerLLDAlloc<S>) struct;

		if (allocate(enclosing)) {
			allocateStructPtr(
					typeDataPtr(enclosing),
					llvmStruct.getTypePtr());
		}

		return new StructRecLLDAlloc<S>(container(enclosing), llvmStruct.getType());
	}

	@Override
	public DataAllocation<RelRecOp> allocateRelPtr(
			DataAllocation<?> enclosing,
			RelRec data,
			DataAllocation<RelRecOp> type) {
		if (allocate(enclosing)) {
			allocateRelPtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new RelRecLLDAlloc(container(enclosing));
	}

	public final DataLayout int8layout() {
		if (this.int8layout != null) {
			return this.int8layout;
		}
		return this.int8layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 8));
	}

	public final DataLayout int16layout() {
		if (this.int16layout != null) {
			return this.int16layout;
		}
		return this.int16layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 16));
	}

	public final DataLayout int32layout() {
		if (this.int32layout != null) {
			return this.int32layout;
		}
		return this.int32layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 32));
	}

	public final DataLayout int64layout() {
		if (this.int64layout != null) {
			return this.int64layout;
		}
		return this.int64layout =
				new DataLayout(intLayout(getModulePtr(), (byte) 64));
	}

	public final DataLayout fp32layout() {
		if (this.fp32layout != null) {
			return this.fp32layout;
		}
		return this.fp32layout = new DataLayout(fp32layout(getModulePtr()));
	}

	public final DataLayout fp64layout() {
		if (this.fp64layout != null) {
			return this.fp64layout;
		}
		return this.fp64layout = new DataLayout(fp64layout(getModulePtr()));
	}

	public final DataLayout ptrLayout() {
		if (this.ptrLayout != null) {
			return this.ptrLayout;
		}
		return this.ptrLayout = new DataLayout(ptrLayout(getModulePtr()));
	}

	public final DataLayout relPtrLayout() {
		if (this.relPtrLayout != null) {
			return this.relPtrLayout;
		}
		return this.relPtrLayout = new DataLayout(relPtrLayout(getModulePtr()));
	}

	public final DataLayout structLayout(ContainerLLDAlloc<?> type) {
		return new DataLayout(structLayout(getModulePtr(), type.getTypePtr()));
	}

	@Override
	public String toString() {
		return "LLVM data allocator";
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
		return ((ContainerLLDAlloc<?>) allocation).getTypePtr();
	}

	private static native long binaryConstant(
			long modulePtr,
			long id,
			int idLen,
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
			long id,
			int idLen,
			long typePtr,
			boolean constant,
			boolean exported);

	private static native long refineType(
			long modulePtr,
			long id,
			int idLen,
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
