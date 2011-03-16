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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLVMCode.nativePtr;
import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public abstract class LLVMRecOp<O extends Op>
		extends LLVMPtrOp
		implements RecOp<O> {

	public LLVMRecOp(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public final O load(Code code) {

		final long nextPtr = nextPtr(code);

		return createLoaded(nextPtr, load(nextPtr, getNativePtr()));
	}

	@Override
	public final void store(Code code, O value) {
		store(nextPtr(code), getNativePtr(), nativePtr(value));
	}

	protected abstract O createLoaded(long blockPtr, long nativePtr);

	public static final class Any extends LLVMRecOp<AnyOp> {

		public Any(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Any create(long blockPtr, long nativePtr) {
			return new Any(blockPtr, nativePtr);
		}

		@Override
		protected AnyOp createLoaded(long blockPtr, long nativePtr) {
			return new LLVMAnyOp(blockPtr, nativePtr);
		}

	}

	public static final class Data extends LLVMRecOp<DataOp> {

		public Data(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Data create(long blockPtr, long nativePtr) {
			return new Data(blockPtr, nativePtr);
		}

		@Override
		protected DataOp createLoaded(long blockPtr, long nativePtr) {
			return new LLVMAnyOp(blockPtr, nativePtr);
		}

	}

	public static final class Int32 extends LLVMRecOp<Int32op> {

		public Int32(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Int32 create(long blockPtr, long nativePtr) {
			return new Int32(blockPtr, nativePtr);
		}

		@Override
		protected Int32op createLoaded(long blockPtr, long nativePtr) {
			return new LLVMInt32op(blockPtr, nativePtr);
		}

	}

	public static final class Int64 extends LLVMRecOp<Int64op> {

		public Int64(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Int64 create(long blockPtr, long nativePtr) {
			return new Int64(blockPtr, nativePtr);
		}

		@Override
		protected Int64op createLoaded(long blockPtr, long nativePtr) {
			return new LLVMInt64op(blockPtr, nativePtr);
		}

	}

	public static final class Fp64 extends LLVMRecOp<Fp64op> {

		public Fp64(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Fp64 create(long blockPtr, long nativePtr) {
			return new Fp64(blockPtr, nativePtr);
		}

		@Override
		protected Fp64op createLoaded(long blockPtr, long nativePtr) {
			return new LLVMFp64op(blockPtr, nativePtr);
		}

	}

	public static final class Rel extends LLVMRecOp<RelOp> {

		public Rel(long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
		}

		@Override
		public Rel create(long blockPtr, long nativePtr) {
			return new Rel(blockPtr, nativePtr);
		}

		@Override
		protected RelOp createLoaded(long blockPtr, long nativePtr) {
			return new LLVMRelOp(blockPtr, nativePtr);
		}

	}

	public static final class Struct<O extends StructOp> extends LLVMRecOp<O> {

		private final Type<O> type;

		public Struct(Type<O> type, long blockPtr, long nativePtr) {
			super(blockPtr, nativePtr);
			this.type = type;
		}

		@Override
		public Struct<O> create(long blockPtr, long nativePtr) {
			return new Struct<O>(this.type, blockPtr, nativePtr);
		}

		@Override
		protected O createLoaded(long blockPtr, long nativePtr) {
			return this.type.op(
					new LLVMStruct(this.type, blockPtr, nativePtr));
		}

	}

}
