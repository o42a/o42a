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

import static org.o42a.backend.llvm.data.LLVMDataAllocator.container;

import java.util.function.Supplier;

import org.o42a.backend.llvm.data.alloc.*;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;


public class LLVMDataWriter implements DataWriter {

	private final LLVMModule module;
	private LLVMData currentData;

	LLVMDataWriter(LLVMModule module) {
		this.module = module;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	@Override
	public DataAllocation<AnyOp> nullPtr(Ptr<AnyOp> pointer) {
		return new AnyLLDAlloc(
				getModule(),
				LLVMId.nullId(nullPtr(getModule().getNativePtr()), false),
				null);
	}

	@Override
	public DataAllocation<DataOp> nullDataPtr(Ptr<DataOp> pointer) {
		return new DataLLDAlloc(
				getModule(),
				LLVMId.nullId(nullPtr(getModule().getNativePtr()), false),
				null);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> nullPtr(
			Ptr<S> pointer,
			Type<S> type) {

		final ContainerLLDAlloc<S> typeAlloc =
				(ContainerLLDAlloc<S>) type.pointer(type.getGenerator())
				.getAllocation();
		final long nativePtr = nullStructPtr(typeAlloc.getTypePtr());

		return new NullStructLLDAlloc<>(
				getModule(),
				nativePtr,
				type);
	}

	@Override
	public <F extends Func<F>> LLFAlloc<F> nullPtr(FuncPtr<F> pointer) {

		final Signature<F> signature = pointer.getSignature();

		return new LLFAlloc<>(
				this.module,
				nullFuncPtr(getModule().nativePtr(signature)),
				signature);
	}

	@Override
	public <S extends StructOp<S>> void begin(
			DataAllocation<S> destination,
			Global<S, ?> global) {
		push(
				destination,
				createStruct(global.getInstance().size(global.getGenerator())));
	}

	@Override
	public <S extends StructOp<S>> void enter(
			DataAllocation<S> destination,
			SubData<S> data) {
		push(destination, createStruct(data.size()));
	}

	@Override
	public <S extends StructOp<S>> void exit(
			DataAllocation<S> destination,
			SubData<S> data) {

		ContainerLLDAlloc<S> dest = (ContainerLLDAlloc<S>) destination;
		final long dataPtr = pull().getNativePtr();

		writeStruct(getStructPtr(), dest.getTypePtr(), dataPtr);
	}

	@Override
	public <S extends StructOp<S>> void end(
			DataAllocation<S> destination,
			Global<S, ?> global) {

		final GlobalLLDAlloc<S> dest =
				(GlobalLLDAlloc<S>) destination;
		final long dataPtr = pull().getNativePtr();

		writeGlobal(dest.getNativePtr(), dataPtr);
	}

	@Override
	public void writeInt8(
			DataAllocation<Int8recOp> destination,
			Supplier<Byte> value) {
		nextField(destination);
		writeInt(
				getModule().getNativePtr(),
				getStructPtr(),
				value.get(),
				8);
	}

	@Override
	public void writeInt16(
			DataAllocation<Int16recOp> destination,
			Supplier<Short> value) {
		nextField(destination);
		writeInt(
				getModule().getNativePtr(),
				getStructPtr(),
				value.get(),
				16);
	}

	@Override
	public void writeInt32(
			DataAllocation<Int32recOp> destination,
			Supplier<Integer> value) {
		nextField(destination);
		writeInt(
				getModule().getNativePtr(),
				getStructPtr(),
				value.get(),
				32);
	}

	@Override
	public void writeInt64(
			DataAllocation<Int64recOp> destination,
			Supplier<Long> value) {
		nextField(destination);
		writeInt(
				getModule().getNativePtr(),
				getStructPtr(),
				value.get(),
				64);
	}

	@Override
	public void writeNativePtrAsInt64(
			DataAllocation<Int64recOp> destination,
			Supplier<Ptr<AnyOp>> value) {
		nextField(destination);

		final LLDAlloc<?> alloc =
				(LLDAlloc<?>) value.get().getAllocation();

		writePtrAsInt64(
				getModule().getNativePtr(), getStructPtr(),
				alloc.llvmId().expression(getModule()));
	}

	@Override
	public void writeFp32(
			DataAllocation<Fp32recOp> destination,
			Supplier<Float> value) {
		nextField(destination);
		writeFp32(getModule().getNativePtr(), getStructPtr(), value.get());
	}

	@Override
	public void writeFp64(
			DataAllocation<Fp64recOp> destination,
			Supplier<Double> value) {
		nextField(destination);
		writeFp64(getModule().getNativePtr(), getStructPtr(), value.get());
	}

	@Override
	public void writeSystem(DataAllocation<SystemOp> destination) {
		nextField(destination);

		final SystemLLDAlloc alloc = (SystemLLDAlloc) destination;
		final SystemTypeLLAlloc typeAlloc = alloc.getTypeAlloc();

		if (typeAlloc.exists()) {
			writeSystemStruct(getStructPtr(), typeAlloc.getTypePtr());
		}
	}

	public final void writeDataId(
			DataAllocation<?> destination,
			LLVMId llvmId) {
		nextField(destination);

		final long ptr = llvmId.expression(getModule());

		writeDataPtr(getStructPtr(), ptr);
	}

	public final void writeCodeId(
			DataAllocation<?> destination,
			LLVMId llvmId) {
		nextField(destination);

		final long ptr = llvmId.expression(getModule());

		writeFuncPtr(getStructPtr(), ptr);
	}

	public final void writeRelPtr(
			DataAllocation<RelRecOp> destination,
			long nativePtr) {
		nextField(destination);
		writeRelPtr(getStructPtr(), nativePtr);
	}

	@Override
	public String toString() {
		return "LLVM data writer";
	}

	private long getStructPtr() {
		return this.currentData.getNativePtr();
	}

	private final void nextField(DataAllocation<?> destination) {
		this.currentData.nextField((LLDAlloc<?>) destination);
	}

	private void push(DataAllocation<?> container, long dataPtr) {

		final ContainerLLDAlloc<?> alloc = container(container);

		if (this.currentData != null) {
			this.currentData.nextField(alloc);
		}

		this.currentData = new LLVMData(alloc, dataPtr, this.currentData);
	}

	private LLVMData pull() {

		final LLVMData pulled = this.currentData;

		this.currentData = this.currentData.getPrev();

		return pulled;
	}

	private static native long nullPtr(long modulePtr);

	private static native long nullStructPtr(long typePtr);

	private static native long nullFuncPtr(long funcTypePtr);

	private static native long createStruct(int size);

	private static native void writeInt(
			long modulePtr,
			long structPtr,
			long value,
			int numBits);

	private static native void writePtrAsInt64(
			long modulePtr,
			long structPtr,
			long value);

	private static native void writeFp32(
			long modulePtr,
			long structPtr,
			float value);

	private static native void writeFp64(
			long modulePtr,
			long structPtr,
			double value);

	private static native void writeFuncPtr(long structPtr, long funcPtr);

	private static native void writeDataPtr(long structPtr, long dataPtr);

	private static native void writeRelPtr(long structPtr, long relPtr);

	private static native void writeStruct(
			long enclosingPtr,
			long typePtr,
			long dataPtr);

	private static native void writeSystemStruct(
			long structPtr,
			long typePtr);

	private static native void writeGlobal(long globalPtr, long dataPtr);

	private static native void writeAlignmentGap(
			long typePtr,
			long dataPtr,
			int index);

	private final class LLVMData {

		private final ContainerLLDAlloc<?> container;
		private final long nativePtr;
		private final LLVMData prev;
		private int lastIndex = -1;

		LLVMData(
				ContainerLLDAlloc<?> container,
				long nativePtr,
				LLVMData prev) {
			this.container = container;
			this.nativePtr = nativePtr;
			this.prev = prev;
		}

		public final ContainerLLDAlloc<?> getContainer() {
			return this.container;
		}

		public final long getNativePtr() {
			return this.nativePtr;
		}

		public final LLVMData getPrev() {
			return this.prev;
		}

		public final void nextField(LLDAlloc<?> field) {

			final int nextIndex = field.llvmId().getIndex();

			for (;;) {
				++this.lastIndex;
				if (this.lastIndex == nextIndex) {
					break;
				}
				assert this.lastIndex < nextIndex :
					"Wrong field " + field + " writing after #"
					+ (this.lastIndex - 1);
				writeAlignmentGap(
						getContainer().getTypePtr(),
						getNativePtr(),
						this.lastIndex);
			}
		}
	}

}
