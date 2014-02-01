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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.util.string.ID;


public abstract class PtrLLOp<P extends PtrOp<P>> implements LLOp<P>, PtrOp<P> {

	private final long blockPtr;
	private final long nativePtr;
	private final ID id;

	public PtrLLOp(ID id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final ID getId() {
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
	public void returnValue(Block code) {
		llvm(code).returnValue(this);
	}

	@Override
	public BoolLLOp isNull(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().unaryId(id, IS_NULL_ID, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(nextPtr, isNull(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public BoolLLOp eq(ID id, Code code, P other) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().binaryId(id, EQ_ID, this, other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(nextPtr, IntLLOp.eq(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolOp ne(ID id, Code code, P other) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().binaryId(id, NE_ID, this, other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(nextPtr, IntLLOp.ne(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected static native long field(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			int field);

	protected static native long load(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			int atomicity);

	protected static native long store(
			long blockPtr,
			long instrPtr,
			long pointerPtr,
			long valuePtr,
			int atomicity);

	protected static native long testAndSet(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			long expectedPtr,
			long valuePtr);

	protected static native long atomicRMW(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			int rmwKind,
			long operandPtr);

	static native long toAny(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toPtr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toInt(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			byte numBits);

	static native long toFp32(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toFp64(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long toRelPtr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long castStructTo(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			long typePtr);

	protected static native long castFuncTo(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			long funcTypePtr);

	static native long isNull(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr);

	static native long offset(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long pointerPtr,
			long indexPtr);

}
