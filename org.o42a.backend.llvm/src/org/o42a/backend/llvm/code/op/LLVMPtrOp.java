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
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public abstract class LLVMPtrOp implements LLVMOp, PtrOp {

	private final long blockPtr;
	private final long nativePtr;
	private final CodeId id;

	public LLVMPtrOp(CodeId id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final CodeId getId() {
		return this.id;
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
	public void allocated(Code code, StructOp enclosing) {
	}

	@Override
	public void returnValue(Code code) {
		llvm(code).returnValue(this);
	}

	@Override
	public LLVMBoolOp isNull(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = LLVMCode.unaryId(this, name, code, "is_null");

		return new LLVMBoolOp(
				id,
				nextPtr,
				isNull(nextPtr, id.getId(), getNativePtr()));
	}

	@Override
	public LLVMBoolOp eq(String name, Code code, PtrOp other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = LLVMCode.binaryId(this, name, code, "eq", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				LLVMIntOp.eq(
						nextPtr,
						id.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMAnyOp toAny(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "any");

		return new LLVMAnyOp(
				id,
				nextPtr,
				toAny(nextPtr, id.getId(), getNativePtr()));
	}

	public final LLVMDataOp toData(String name, Code code) {
		return toData(castId(name, code, "struct"), code);
	}

	public LLVMDataOp toData(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp(
				id,
				nextPtr,
				toAny(nextPtr, id.getId(), getNativePtr()));
	}

	public final <O extends StructOp> O to(
			String name,
			Code code,
			Type<O> type) {
		return to(castId(name, code, type.getType().getId()), code, type);
	}

	public <O extends StructOp> O to(CodeId id, Code code, Type<O> type) {

		final long nextPtr = nextPtr(code);

		return type.op(new LLVMStruct(
				id,
				type,
				nextPtr,
				castStructTo(
						nextPtr,
						id.getId(),
						getNativePtr(),
						typePtr(type))));
	}

	public final <F extends Func> LLVMFuncOp<F> toFunc(
			String name,
			Code code,
			Signature<F> signature) {
		return toFunc(castId(name, code, signature.getId()), code, signature);
	}

	public <F extends Func> LLVMFuncOp<F> toFunc(
			CodeId id,
			Code code,
			Signature<F> signature) {

		final LLVMCode llvm = LLVMCode.llvm(code);
		final long nextPtr = llvm.nextPtr();

		return new LLVMFuncOp<F>(
				id,
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

	protected final CodeId castId(String name, Code code, String suffix) {
		return LLVMCode.castId(this, name, code, suffix);
	}

	protected final CodeId castId(String name, Code code, CodeId suffix) {
		return LLVMCode.castId(this, name, code, suffix);
	}

	protected final CodeId derefId(String name, Code code) {
		if (name != null) {
			return code.nameId(name);
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

}
