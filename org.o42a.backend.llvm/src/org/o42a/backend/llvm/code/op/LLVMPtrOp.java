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

import static org.o42a.backend.llvm.code.LLVMCode.*;

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public abstract class LLVMPtrOp<P extends PtrOp<P>>
		implements LLVMOp<P>, PtrOp<P> {

	private final long blockPtr;
	private final long nativePtr;
	private final CodeId id;
	private final AllocClass allocClass;

	public LLVMPtrOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		this.id = id;
		this.allocClass = allocClass;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	@Override
	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	@Override
	public final long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public final long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public void allocated(Code code, StructOp<?> enclosing) {
	}

	@Override
	public void returnValue(Code code) {
		llvm(code).returnValue(this);
	}

	@Override
	public LLVMBoolOp isNull(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = LLVMCode.unaryId(this, id, code, "is_null");

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				isNull(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public LLVMBoolOp eq(CodeId id, Code code, P other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId =
				LLVMCode.binaryId(this, id, code, "eq", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				LLVMIntOp.eq(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public P offset(CodeId id, Code code, IntOp<?> index) {

		final long nextPtr = nextPtr(code);
		final CodeId offsetId =
				id != null
				? id : getId().detail("offset_by").detail(index.getId());

		return create(
				offsetId,
				nextPtr,
				offset(
						nextPtr,
						offsetId.getId(),
						getNativePtr(),
						nativePtr(index)));
	}

	@Override
	public LLVMAnyOp toAny(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId castId = castId(id, code, "any");

		return new LLVMAnyOp(
				castId,
				getAllocClass(),
				nextPtr,
				toAny(nextPtr, castId.getId(), getNativePtr()));
	}

	public LLVMDataOp toData(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId castId = castId(id, code, "struct");

		return new LLVMDataOp(
				castId,
				getAllocClass(),
				nextPtr,
				toAny(nextPtr, castId.getId(), getNativePtr()));
	}

	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {

		final long nextPtr = nextPtr(code);
		final CodeId castId = castId(id, code, type.getId());

		return type.op(new LLVMStruct<SS>(
				castId,
				getAllocClass(),
				type,
				nextPtr,
				castStructTo(
						nextPtr,
						castId.getId(),
						getNativePtr(),
						typePtr(type))));
	}

	public <F extends Func<F>> LLVMFuncOp<F> toFunc(
			CodeId id,
			Code code,
			Signature<F> signature) {

		final LLVMCode llvm = LLVMCode.llvm(code);
		final long nextPtr = llvm.nextPtr();

		return new LLVMFuncOp<F>(
				id,
				getAllocClass(),
				nextPtr,
				castFuncTo(
						nextPtr,
						id.getId(),
						getNativePtr(),
						llvm.getModule().nativePtr(signature)),
				signature);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected final CodeId castId(CodeId id, Code code, String suffix) {
		return LLVMCode.castId(this, id, code, suffix);
	}

	protected final CodeId castId(CodeId id, Code code, CodeId suffix) {
		return LLVMCode.castId(this, id, code, suffix);
	}

	protected final CodeId derefId(CodeId id, Code code) {
		if (id != null) {
			return code.opId(id);
		}
		return getId().detail("deref");
	}

	protected static native long field(
			long blockPtr,
			String id,
			long pointerPtr,
			int field);

	static native long load(long blockPtr, String id, long pointerPtr);

	static native void store(
			long blockPtr,
			long pointerPtr,
			long valuePtr);

	static native long toAny(long blockPtr, String id, long pointerPtr);

	static native long toPtr(long blockPtr, String id, long pointerPtr);

	static native long toInt(
			long blockPtr,
			String id,
			long pointerPtr,
			byte numBits);

	static native long toFp32(long blockPtr, String id, long pointerPtr);

	static native long toFp64(long blockPtr, String id, long pointerPtr);

	static native long toRelPtr(long blockPtr, String id, long pointerPtr);

	private static native long castStructTo(
			long blockPtr,
			String id,
			long pointerPtr,
			long typePtr);

	private static native long castFuncTo(
			long blockPtr,
			String id,
			long pointerPtr,
			long funcTypePtr);

	private static native long isNull(
			long blockPtr,
			String id,
			long pointerPtr);

	private static native long offset(
			long blockPtr,
			String id,
			long pointerPtr,
			long indexPtr);

}
