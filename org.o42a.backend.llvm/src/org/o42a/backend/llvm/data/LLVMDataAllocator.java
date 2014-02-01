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
package org.o42a.backend.llvm.data;

import static org.o42a.backend.llvm.data.NameLLVMEncoder.NAME_LLVM_ENCODER;
import static org.o42a.backend.llvm.data.SystemTypeInfo.allocateSystemType;
import static org.o42a.backend.llvm.id.LLVMId.dataId;

import org.o42a.backend.llvm.data.alloc.*;
import org.o42a.backend.llvm.data.rec.*;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


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
	private DataLayout fp128layout;
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
			Ptr<AnyOp> pointer,
			byte[] data,
			int start,
			int end) {

		final ID id = pointer.getId();
		final NativeBuffer ids = getModule().ids();
		final long nativePtr = binaryConstant(
				getModulePtr(),
				ids.write(id),
				ids.length(),
				data,
				start,
				end,
				pointer.isPtrToConstant());

		return new AnyLLDAlloc(getModule(), dataId(id, nativePtr), null);
	}

	@Override
	public DataAllocation<SystemOp> addSystemType(SystemType systemType) {
		return allocateSystemType(this, systemType);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			Type<S> type) {

		final NativeBuffer ids = getModule().ids();

		return new TypeLLAlloc<>(
				getModule(),
				createType(
						getModulePtr(),
						ids.write(type.getId()),
						ids.length()),
				createTypeData(getModulePtr()),
				type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			DataAllocation<S> type,
			Global<S, ?> global) {

		final long typePtr;
		final long typeDataPtr;

		if (type != null) {
			typePtr = typePtr(type);
			typeDataPtr = 0L;
		} else {

			final NativeBuffer ids = getModule().ids();

			typePtr = createType(
					getModulePtr(),
					ids.write(global.getId().detail("type")),
					ids.length());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return new GlobalLLDAlloc<>(
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

			final NativeBuffer ids = getModule().ids();

			typePtr = createType(
					getModulePtr(),
					ids.write(data.getInstance().getId()),
					ids.length());
			typeDataPtr = createTypeData(getModulePtr());
		}

		return new StructLLDAlloc<>(
				typePtr,
				typeDataPtr,
				container(enclosing),
				data.getInstance(),
				data.getPointer().getProtoAllocation(),
				NAME_LLVM_ENCODER.print(data.getId()));
	}

	@Override
	public void exit(DataAllocation<?> enclosing, SubData<?> data) {

		final ContainerLLDAlloc<?> allocation =
				container(data.getPointer().getAllocation());

		if (!allocation.isTypeAllocated()) {
			refineType(
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					data.getInstance().isPacked());
			allocation.typeAllocated();
		}
		allocation.setNativePtr(allocation.getTypePtr());
		if (allocate(enclosing)) {
			allocateStruct(
					getModulePtr(),
					typeDataPtr(enclosing),
					allocation.getTypePtr());
		}
	}

	@Override
	public void end(Global<?, ?> global) {

		final GlobalLLDAlloc<?> allocation =
				(GlobalLLDAlloc<?>)
				global.getPointer().getAllocation();

		assert allocation.llvmId().getGlobalId().equals(global.getId()) :
			"Error closing global data " + global;

		if (!allocation.isTypeAllocated()) {
			refineType(
					allocation.getTypePtr(),
					allocation.getTypeDataPtr(),
					global.getInstance().isPacked());
			allocation.typeAllocated();
		}

		final NativeBuffer ids = getModule().ids();

		allocation.setNativePtr(allocateGlobal(
				getModulePtr(),
				ids.write(global.getId()),
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

		refineType(
				allocation.getTypePtr(),
				allocation.getTypeDataPtr(),
				type.isPacked());
		allocation.typeAllocated();
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
		return new Int8lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<Int16recOp> allocateInt16(
			DataAllocation<?> enclosing,
			Int16rec data,
			DataAllocation<Int16recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 16);
		}
		return new Int16lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<Int32recOp> allocateInt32(
			DataAllocation<?> enclosing,
			Int32rec data,
			DataAllocation<Int32recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 32);
		}
		return new Int32lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<Int64recOp> allocateInt64(
			DataAllocation<?> enclosing,
			Int64rec data,
			DataAllocation<Int64recOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), (byte) 64);
		}
		return new Int64lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<Fp32recOp> allocateFp32(
			DataAllocation<?> enclosing,
			Fp32rec data,
			DataAllocation<Fp32recOp> type) {
		if (allocate(enclosing)) {
			allocateFp32(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp32lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<Fp64recOp> allocateFp64(
			DataAllocation<?> enclosing,
			Fp64rec data,
			DataAllocation<Fp64recOp> type) {
		if (allocate(enclosing)) {
			allocateFp64(getModulePtr(), typeDataPtr(enclosing));
		}
		return new Fp64lldAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<SystemOp> allocateSystem(
			DataAllocation<?> enclosing,
			SystemData data,
			DataAllocation<SystemOp> type) {

		final SystemTypeLLAlloc typeAlloc =
				(SystemTypeLLAlloc) data.getSystemType().getAllocation();

		if (typeAlloc.exists() && allocate(enclosing)) {
			allocateStruct(
					getModulePtr(),
					typeDataPtr(enclosing),
					typeAlloc.getTypePtr());
		}

		return new SystemLLDAlloc(container(enclosing), type, typeAlloc);
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
		return new FuncRecLLDAlloc<>(container(enclosing), type, signature);
	}

	@Override
	public DataAllocation<AnyRecOp> allocatePtr(
			DataAllocation<?> enclosing,
			AnyRec data,
			DataAllocation<AnyRecOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new AnyRecLLDAlloc(container(enclosing), type);
	}

	@Override
	public DataAllocation<DataRecOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataRec data,
			DataAllocation<DataRecOp> type) {
		if (allocate(enclosing)) {
			allocatePtr(getModulePtr(), typeDataPtr(enclosing));
		}
		return new DataRecLLDAlloc(container(enclosing), type);
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

		return new StructRecLLDAlloc<>(
				container(enclosing),
				type,
				llvmStruct.getType());
	}

	@Override
	public DataAllocation<RelRecOp> allocateRelPtr(
			DataAllocation<?> enclosing,
			RelRec data,
			DataAllocation<RelRecOp> type) {
		if (allocate(enclosing)) {
			allocateInt(getModulePtr(), typeDataPtr(enclosing), 32);
		}
		return new RelRecLLDAlloc(container(enclosing), type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> externStruct(
			Ptr<S> pointer,
			DataAllocation<S> type,
			GlobalAttributes attributes) {

		final ContainerLLDAlloc<S> typeAlloc = (ContainerLLDAlloc<S>) type;
		final GlobalLLDAlloc<S> global = new GlobalLLDAlloc<>(
				getModule(),
				typeAlloc.getTypePtr(),
				0L,
				pointer.getId(),
				typeAlloc.getType());
		final NativeBuffer ids = getModule().ids();

		global.setNativePtr(externStruct(
				getModulePtr(),
				ids.write(pointer.getId()),
				ids.length(),
				global.getTypePtr(),
				attributes.isConstant()));

		return global;
	}

	public final DataLayout int8layout() {
		if (this.int8layout != null) {
			return this.int8layout;
		}
		return this.int8layout =
				new DataLayout(intLayout(getModulePtr(), 8));
	}

	public final DataLayout int16layout() {
		if (this.int16layout != null) {
			return this.int16layout;
		}
		return this.int16layout =
				new DataLayout(intLayout(getModulePtr(), 16));
	}

	public final DataLayout int32layout() {
		if (this.int32layout != null) {
			return this.int32layout;
		}
		return this.int32layout =
				new DataLayout(intLayout(getModulePtr(), 32));
	}

	public final DataLayout int64layout() {
		if (this.int64layout != null) {
			return this.int64layout;
		}
		return this.int64layout =
				new DataLayout(intLayout(getModulePtr(), 64));
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

	public final DataLayout fp128layout() {
		if (this.fp128layout != null) {
			return this.fp128layout;
		}
		return this.fp128layout = new DataLayout(fp128layout(getModulePtr()));
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

	/**
	 * Allocates the field of the given size with the same alignment.
	 *
	 * @param enclosingPtr enclosing type's data native pointer.
	 * @param numBytes number of bytes to allocate and an alignment
	 * of the allocated data.
	 * @return <code>true</code> if alignment succeed, or <code>false</code>
	 * if the field of the given size and alignment can not be allocated.
	 */
	public boolean allocateField(long enclosingPtr, int numBytes) {

		final long modulePtr = getModule().getNativePtr();

		if (allocatePtrField(modulePtr, enclosingPtr, numBytes)) {
			return true;
		}
		if (allocateIntField(modulePtr, enclosingPtr, numBytes)) {
			return true;
		}
		if (allocateFpField(modulePtr, enclosingPtr, numBytes)) {
			return true;
		}

		return false;
	}

	private boolean allocatePtrField(
			long modulePtr,
			long enclosingPtr,
			int numBytes) {
		if (ptrLayout().alignment().getBytes() != numBytes) {
			return false;
		}
		allocatePtr(modulePtr, enclosingPtr);
		return true;
	}

	private boolean allocateIntField(
			long modulePtr,
			long enclosingPtr,
			int numBytes) {
		// An alignment of the integer type may differ from it's size.
		// In this case the integer can not be used to ensure the proper
		// alignment.
		if (numBytes != 1) {

			final int alignment;

			switch (numBytes) {
			case 8:
				alignment = int64layout().alignment().getBytes();
				break;
			case 4:
				alignment = int32layout().alignment().getBytes();
				break;
			case 2:
				alignment = int16layout().alignment().getBytes();
				break;
			default:
				alignment =
						new DataLayout(intLayout(modulePtr, numBytes << 3))
						.alignment()
						.getBytes();
			}

			if (alignment != numBytes) {
				return false;
			}
		}

		allocateInt(modulePtr, enclosingPtr, (short) (numBytes << 3));

		return true;
	}

	private boolean allocateFpField(
			long modulePtr,
			long enclosingPtr,
			int numBytes) {
		switch (numBytes) {
		case 8:
			if (fp64layout().alignment().getBytes() != numBytes) {
				return false;
			}
			allocateFp64(modulePtr, enclosingPtr);
			return true;
		case 16:
			if (fp128layout().alignment().getBytes() != numBytes) {
				return false;
			}
			allocateFp128(modulePtr, enclosingPtr);
			return true;
		case 4:
			if (fp32layout().alignment().getBytes() != numBytes) {
				return false;
			}
			allocateFp32(modulePtr, enclosingPtr);
			return true;
		}

		return false;
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

	static native long createType(
			long modulePtr,
			long id,
			int idLen);

	static native long createTypeData(long modulePtr);

	private static native void allocateStruct(
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

	static native void refineType(
			long typePtr,
			long typeDataPtr,
			boolean packed);

	private static native void allocateInt(
			long modulePtr,
			long enclosingPtr,
			int numBits);

	private static native void allocateFp32(long modulePtr, long enclosingPtr);

	private static native void allocateFp64(long modulePtr, long enclosingPtr);

	private static native void allocateFp128(long modulePtr, long enclosingPtr);

	private static native void allocateFuncPtr(
			long enclosingPtr,
			long functTypePtr);

	private static native void allocatePtr(long modulePtr, long enclosingPtr);

	private static native void allocateStructPtr(
			long enclosingPtr,
			long typePtr);

	private static native long externStruct(
			long modulePtr,
			long id,
			int idLen,
			long typePtr,
			boolean constant);

	private static native int intLayout(long modulePtr, int numBits);

	private static native int fp32layout(long modulePtr);

	private static native int fp64layout(long modulePtr);

	private static native int fp128layout(long modulePtr);

	private static native int ptrLayout(long modulePtr);

	private static native int relPtrLayout(long modulePtr);

	static native int structLayout(long modulePtr, long typePtr);

	public static native void dumpStructLayout(long modulePtr, long typePtr);

}
