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

import static org.o42a.backend.llvm.code.LLVMCode.nativePtr;

import org.o42a.backend.llvm.code.FuncAllocation;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;
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
	public DataAllocation<AnyOp> nullPtr() {
		return new AnyAlloc(
				getModule(),
				LLVMId.nullId(nullPtr(getModule().getNativePtr()), false),
				null);
	}

	@Override
	public DataAllocation<DataOp> nullDataPtr() {
		return new AnyDataAlloc(
				getModule(),
				LLVMId.nullId(nullPtr(getModule().getNativePtr()), false),
				null);
	}

	@Override
	public <O extends StructOp> DataAllocation<O> nullPtr(Type<O> type) {

		final ContainerAllocation<O> typeAlloc =
			(ContainerAllocation<O>) type.pointer(type.getGenerator())
			.getAllocation();
		final long nativePtr = nullStructPtr(typeAlloc.getTypePtr());

		return new ContainerAllocation.Null<O>(
				getModule(),
				nativePtr,
				type);
	}

	@Override
	public <F extends Func> FuncAllocation<F> nullPtr(Signature<F> signature) {
		return new FuncAllocation<F>(
				this.module,
				nullFuncPtr(nativePtr(signature)),
				signature);
	}

	@Override
	public <O extends StructOp> void begin(
			DataAllocation<O> allocation,
			Global<O, ?> global) {
		push(createStruct(global.getInstance().size(global.getGenerator())));
	}

	@Override
	public <O extends StructOp> void enter(
			DataAllocation<O> allocation,
			SubData<O> data) {
		push(createStruct(data.size()));
	}

	@Override
	public <O extends StructOp> void exit(
			DataAllocation<O> allocation,
			SubData<O> data) {

		ContainerAllocation<O> alloc = (ContainerAllocation<O>) allocation;
		final long dataPtr = pull().getNativePtr();

		writeStruct(getStructPtr(), alloc.getTypePtr(), dataPtr);
	}

	@Override
	public <O extends StructOp> void end(
			DataAllocation<O> allocation,
			Global<O, ?> global) {

		ContainerAllocation.Global<O> alloc =
			(ContainerAllocation.Global<O>) allocation;
		final long dataPtr = pull().getNativePtr();

		writeGlobal(alloc.getNativePtr(), dataPtr);
	}

	@Override
	public void writeInt32(
			DataAllocation<RecOp<Int32op>> allocation,
			int value) {
		writeInt32(getModule().getNativePtr(), getStructPtr(), value);
	}

	@Override
	public void writeInt64(
			DataAllocation<RecOp<Int64op>> allocation,
			long value) {
		writeInt64(getModule().getNativePtr(), getStructPtr(), value);
	}

	@Override
	public void writeNativePtrAsInt64(
			DataAllocation<RecOp<Int64op>> allocation,
			DataAllocation<AnyOp> valueAllocation) {

		final LLVMDataAllocation<?> alloc =
			(LLVMDataAllocation<?>) valueAllocation;

		writePtrAsInt64(
				getModule().getNativePtr(), getStructPtr(),
				alloc.llvmId().expression(getModule()));
	}

	@Override
	public void writeFp64(
			DataAllocation<RecOp<Fp64op>> allocation,
			Double value) {
		writeFp64(getModule().getNativePtr(), getStructPtr(), value);
	}

	@Override
	public String toString() {
		return "LLVM data writer";
	}

	final void writeDataId(LLVMId llvmId) {

		final long ptr = llvmId.expression(getModule());

		writeDataPtr(getStructPtr(), ptr);
	}

	final void writeCodeId(LLVMId llvmId) {

		final long ptr = llvmId.expression(getModule());

		writeCodePtr(getStructPtr(), ptr);
	}

	final void writeRelPtr(long nativePtr) {
		writeRelPtr(getStructPtr(), nativePtr);
	}

	private long getStructPtr() {
		return this.currentData.getNativePtr();
	}

	private void push(final long dataPtr) {
		this.currentData = new LLVMData(dataPtr, this.currentData);
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

	private static native void writeInt32(
			long modulePtr,
			long structPtr,
			int value);

	private static native void writeInt64(
			long modulePtr,
			long structPtr,
			long value);

	private static native void writePtrAsInt64(
			long modulePtr,
			long structPtr,
			long value);

	private static native void writeFp64(
			long modulePtr,
			long structPtr,
			double value);

	private static native void writeCodePtr(long structPtr, long codePtr);

	private static native void writeDataPtr(long structPtr, long dataPtr);

	private static native void writeRelPtr(long structPtr, long relPtr);

	private static native void writeStruct(
			long enclosingPtr,
			long typePtr,
			long dataPtr);

	private static native void writeGlobal(long globalPtr, long dataPtr);

	private static final class LLVMData {

		private final long nativePtr;
		private final LLVMData prev;

		public LLVMData(long nativePtr, LLVMData prev) {
			this.nativePtr = nativePtr;
			this.prev = prev;
		}

		public long getNativePtr() {
			return this.nativePtr;
		}

		public LLVMData getPrev() {
			return this.prev;
		}

	}

}
