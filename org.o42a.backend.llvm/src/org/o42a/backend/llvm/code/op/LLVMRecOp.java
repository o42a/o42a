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
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public abstract class LLVMRecOp<O extends Op>
		extends LLVMPtrOp
		implements RecOp<O> {

	public LLVMRecOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public final O load(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = derefId(name, code);

		return createLoaded(
				id,
				nextPtr,
				load(nextPtr, id.getId(), getNativePtr()));
	}

	@Override
	public final void store(Code code, O value) {
		store(nextPtr(code), getNativePtr(), nativePtr(value));
	}

	protected abstract O createLoaded(CodeId id, long blockPtr, long nativePtr);

	public static final class Any extends LLVMRecOp<AnyOp> {

		public Any(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Any create(CodeId id, long blockPtr, long nativePtr) {
			return new Any(id, blockPtr, nativePtr);
		}

		@Override
		protected AnyOp createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMAnyOp(id, blockPtr, nativePtr);
		}

	}

	public static final class Data extends LLVMRecOp<DataOp> {

		public Data(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Data create(CodeId id, long blockPtr, long nativePtr) {
			return new Data(id, blockPtr, nativePtr);
		}

		@Override
		protected DataOp createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMDataOp(id, blockPtr, nativePtr);
		}

	}

	public static final class Int8 extends LLVMRecOp<Int8op> {

		public Int8(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Int8 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int8(id, blockPtr, nativePtr);
		}

		@Override
		protected Int8op createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMInt8op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int16 extends LLVMRecOp<Int16op> {

		public Int16(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Int16 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int16(id, blockPtr, nativePtr);
		}

		@Override
		protected Int16op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt16op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int32 extends LLVMRecOp<Int32op> {

		public Int32(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Int32 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int32(id, blockPtr, nativePtr);
		}

		@Override
		protected Int32op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt32op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int64 extends LLVMRecOp<Int64op> {

		public Int64(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Int64 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int64(id, blockPtr, nativePtr);
		}

		@Override
		protected Int64op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt64op(id, blockPtr, nativePtr);
		}

	}

	public static final class Fp32 extends LLVMRecOp<Fp32op> {

		public Fp32(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Fp32 create(CodeId id, long blockPtr, long nativePtr) {
			return new Fp32(id, blockPtr, nativePtr);
		}

		@Override
		protected Fp32op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMFp32op(id, blockPtr, nativePtr);
		}

	}

	public static final class Fp64 extends LLVMRecOp<Fp64op> {

		public Fp64(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Fp64 create(CodeId id, long blockPtr, long nativePtr) {
			return new Fp64(id, blockPtr, nativePtr);
		}

		@Override
		protected Fp64op createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMFp64op(id, blockPtr, nativePtr);
		}

	}

	public static final class Rel extends LLVMRecOp<RelOp> {

		public Rel(CodeId id, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
		}

		@Override
		public Rel create(CodeId id, long blockPtr, long nativePtr) {
			return new Rel(id, blockPtr, nativePtr);
		}

		@Override
		protected RelOp createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMRelOp(id, blockPtr, nativePtr);
		}

	}

	public static final class Struct<O extends StructOp> extends LLVMRecOp<O> {

		private final Type<O> type;

		public Struct(CodeId id, Type<O> type, long blockPtr, long nativePtr) {
			super(id, blockPtr, nativePtr);
			this.type = type;
		}

		@Override
		public Struct<O> create(CodeId id, long blockPtr, long nativePtr) {
			return new Struct<O>(id, this.type, blockPtr, nativePtr);
		}

		@Override
		protected O createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return this.type.op(
					new LLVMStruct(id, this.type, blockPtr, nativePtr));
		}

	}

}
