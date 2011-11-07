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

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.typePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public abstract class PtrLLOp<P extends PtrOp<P>> implements LLOp<P>, PtrOp<P> {

	private final long blockPtr;
	private final long nativePtr;
	private final CodeId id;
	private final AllocClass allocClass;

	public PtrLLOp(
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
	public BoolLLOp isNull(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = LLCode.unaryId(this, id, code, "is_null");

		return new BoolLLOp(
				resultId,
				nextPtr,
				isNull(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	@Override
	public BoolLLOp eq(CodeId id, Code code, P other) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId =
				LLCode.binaryId(this, id, code, "eq", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				IntLLOp.eq(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public P offset(CodeId id, Code code, IntOp<?> index) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId offsetId =
				id != null
				? id : getId().detail("offset_by").detail(index.getId());

		return create(
				offsetId,
				nextPtr,
				offset(
						nextPtr,
						ids.writeCodeId(offsetId),
						ids.length(),
						getNativePtr(),
						nativePtr(index)));
	}

	@Override
	public AnyLLOp toAny(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = castId(id, code, "any");

		return new AnyLLOp(
				castId,
				getAllocClass(),
				nextPtr,
				toAny(
						nextPtr,
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr()));
	}

	public DataLLOp toData(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = castId(id, code, "struct");

		return new DataLLOp(
				castId,
				getAllocClass(),
				nextPtr,
				toAny(
						nextPtr,
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr()));
	}

	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = castId(id, code, type.getId());

		return type.op(new LLStruct<SS>(
				castId,
				getAllocClass(),
				type,
				nextPtr,
				castStructTo(
						nextPtr,
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr(),
						typePtr(type))));
	}

	public <F extends Func<F>> FuncLLOp<F> toFunc(
			CodeId id,
			Code code,
			Signature<F> signature) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();

		return new FuncLLOp<F>(
				id,
				getAllocClass(),
				nextPtr,
				castFuncTo(
						nextPtr,
						ids.writeCodeId(id),
						ids.length(),
						getNativePtr(),
						llvm.getModule().nativePtr(signature)),
				signature);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected final CodeId castId(CodeId id, Code code, String suffix) {
		return LLCode.castId(this, id, code, suffix);
	}

	protected final CodeId castId(CodeId id, Code code, CodeId suffix) {
		return LLCode.castId(this, id, code, suffix);
	}

	protected final CodeId derefId(CodeId id, Code code) {
		if (id != null) {
			return code.opId(id);
		}
		return getId().detail("deref");
	}

	protected static native long field(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr,
			int field);

	protected static native long load(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	protected static native void store(
			long blockPtr,
			long pointerPtr,
			long valuePtr);

	static native long toAny(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toPtr(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toInt(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr,
			byte numBits);

	static native long toFp32(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toFp64(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toRelPtr(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	private static native long castStructTo(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr,
			long typePtr);

	private static native long castFuncTo(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr,
			long funcTypePtr);

	private static native long isNull(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr);

	private static native long offset(
			long blockPtr,
			long id,
			int idLen,
			long pointerPtr,
			long indexPtr);

}
