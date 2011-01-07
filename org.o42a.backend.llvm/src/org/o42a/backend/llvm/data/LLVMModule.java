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

import org.o42a.backend.llvm.LLVMGenerator;
import org.o42a.backend.llvm.code.LLVMCodeBackend;
import org.o42a.codegen.data.Type;


public final class LLVMModule {

	static {
		System.loadLibrary("o42ac_llvm");
	}

	private final LLVMGenerator generator;
	private final long nativePtr;

	private final LLVMDataAllocator dataAllocator;
	private final LLVMDataWriter dataWriter;
	private final LLVMCodeBackend codeBackend;

	private long voidType;
	private long int32type;
	private long int64type;
	private long fp64type;
	private long boolType;
	private long anyType;

	public LLVMModule(LLVMGenerator generator, String[] commandLine) {
		this.generator = generator;
		this.nativePtr = createModule(generator.getId(), commandLine);
		assert this.nativePtr != 0 :
			"Failed to create LLVM module " + generator.getId();
		this.dataAllocator = new LLVMDataAllocator(this);
		this.dataWriter = new LLVMDataWriter(this);
		this.codeBackend = new LLVMCodeBackend(this);
	}

	public LLVMGenerator getGenerator() {
		return this.generator;
	}

	public long getNativePtr() {
		return this.nativePtr;
	}

	public final LLVMDataAllocator dataAllocator() {
		return this.dataAllocator;
	}

	public final LLVMDataWriter dataWriter() {
		return this.dataWriter;
	}

	public final LLVMCodeBackend codeBackend() {
		return this.codeBackend;
	}

	public long voidType() {
		if (this.voidType != 0L) {
			return this.voidType;
		}
		return this.voidType = voidType(getNativePtr());
	}

	public long int32type() {
		if (this.int32type != 0L) {
			return this.int32type;
		}
		return this.int32type = int32type(getNativePtr());
	}

	public long int64type() {
		if (this.int64type != 0L) {
			return this.int64type;
		}
		return this.int64type = int64type(getNativePtr());
	}

	public long fp64type() {
		if (this.fp64type != 0L) {
			return this.fp64type;
		}
		return this.fp64type = fp64type(getNativePtr());
	}

	public long boolType() {
		if (this.boolType != 0L) {
			return this.boolType;
		}
		return this.boolType = boolType(getNativePtr());
	}

	public long anyType() {
		if (this.anyType != 0L) {
			return this.anyType;
		}
		return this.anyType = anyType(getNativePtr());
	}

	public long pointerTo(Type<?> type) {

		final ContainerAllocation<?> allocation =
			(ContainerAllocation<?>) type.getPointer().getAllocation();

		return pointerTo(allocation.getTypePtr());
	}

	public void write() {
		write(getNativePtr());
	}

	public void destroy() {
		destroyModule(this.nativePtr);
	}

	@Override
	protected void finalize() throws Throwable {
		destroy();
	}

	private static native long createModule(String id, String commandLine[]);

	private static native boolean write(long modulePtr);

	private static native void destroyModule(long modulePtr);

	private static native long voidType(long modulePtr);

	private static native long int32type(long modulePtr);

	private static native long int64type(long modulePtr);

	private static native long fp64type(long modulePtr);

	private static native long boolType(long modulePtr);

	private static native long anyType(long modulePtr);

	private static native long pointerTo(long typePtr);

}
