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
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public abstract class LLVMRecOp<O extends Op>
		extends LLVMPtrOp
		implements RecOp<O> {

	public LLVMRecOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public final O load(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = derefId(id, code);

		return createLoaded(
				resultId,
				nextPtr,
				load(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public final void store(Code code, O value) {
		store(nextPtr(code), getNativePtr(), nativePtr(value));
	}

	protected abstract O createLoaded(CodeId id, long blockPtr, long nativePtr);

	public static final class Any extends LLVMRecOp<AnyOp> implements AnyRecOp {

		public Any(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Any create(CodeId id, long blockPtr, long nativePtr) {
			return new Any(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected AnyOp createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMAnyOp(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

	}

	public static final class Data
			extends LLVMRecOp<DataOp>
			implements DataRecOp {

		public Data(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Data create(CodeId id, long blockPtr, long nativePtr) {
			return new Data(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected DataOp createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMDataOp(
					id,
					AUTO_ALLOC_CLASS,
					blockPtr,
					nativePtr);
		}

	}

	public static final class Int8
			extends LLVMRecOp<Int8op>
			implements Int8recOp {

		public Int8(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Int8 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int8(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Int8op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt8op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int16
			extends LLVMRecOp<Int16op>
			implements Int16recOp {

		public Int16(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Int16 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int16(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Int16op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt16op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int32
			extends LLVMRecOp<Int32op>
			implements Int32recOp {

		public Int32(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Int32 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int32(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Int32op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt32op(id, blockPtr, nativePtr);
		}

	}

	public static final class Int64
			extends LLVMRecOp<Int64op>
			implements Int64recOp {

		public Int64(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Int64 create(CodeId id, long blockPtr, long nativePtr) {
			return new Int64(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Int64op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMInt64op(id, blockPtr, nativePtr);
		}

	}

	public static final class Fp32
			extends LLVMRecOp<Fp32op>
			implements Fp32recOp {

		public Fp32(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Fp32 create(CodeId id, long blockPtr, long nativePtr) {
			return new Fp32(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Fp32op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMFp32op(id, blockPtr, nativePtr);
		}

	}

	public static final class Fp64
			extends LLVMRecOp<Fp64op>
			implements Fp64recOp {

		public Fp64(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Fp64 create(CodeId id, long blockPtr, long nativePtr) {
			return new Fp64(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected Fp64op createLoaded(
				CodeId id,
				long blockPtr,
				long nativePtr) {
			return new LLVMFp64op(id, blockPtr, nativePtr);
		}

	}

	public static final class Rel
			extends LLVMRecOp<RelOp>
			implements RelRecOp {

		public Rel(
				CodeId id,
				AllocClass allocClass,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
		}

		@Override
		public Rel create(CodeId id, long blockPtr, long nativePtr) {
			return new Rel(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
		}

		@Override
		protected RelOp createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return new LLVMRelOp(id, blockPtr, nativePtr);
		}

	}

	public static final class Struct<S extends StructOp<S>>
			extends LLVMRecOp<S>
			implements StructRecOp<S> {

		private final Type<S> type;

		public Struct(
				CodeId id,
				AllocClass allocClass,
				Type<S> type,
				long blockPtr,
				long nativePtr) {
			super(id, allocClass, blockPtr, nativePtr);
			this.type = type;
		}

		@Override
		public Struct<S> create(CodeId id, long blockPtr, long nativePtr) {
			return new Struct<S>(
					id,
					AUTO_ALLOC_CLASS,
					this.type,
					blockPtr,
					nativePtr);
		}

		@Override
		protected S createLoaded(CodeId id, long blockPtr, long nativePtr) {
			return this.type.op(new LLVMStruct<S>(
					id,
					AUTO_ALLOC_CLASS,
					this.type,
					blockPtr,
					nativePtr));
		}

	}

}
