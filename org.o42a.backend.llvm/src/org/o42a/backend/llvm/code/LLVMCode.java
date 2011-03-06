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
package org.o42a.backend.llvm.code;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.data.ContainerAllocation;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeAllocation;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class LLVMCode implements CodeWriter {

	public static final LLVMCode llvm(Code code) {
		return (LLVMCode) code.writer();
	}

	public static final LLVMOp llvm(Op op) {
		if (op instanceof StructOp) {
			return llvm((StructOp) op);
		}
		if (op instanceof Func) {
			return llvm((Func) op);
		}
		return (LLVMOp) op;
	}

	public static final LLVMStruct llvm(StructOp op) {
		return (LLVMStruct) op.writer();
	}

	public static final LLVMFunc<?> llvm(Func func) {
		return (LLVMFunc<?>) func.caller();
	}

	public static final <F extends Func> LLVMSignature<F> llvm(
			Signature<F> signature) {
		return (LLVMSignature<F>) signature.getAllocation();
	}

	public static LLVMCodePos llvm(CodePos pos) {
		return (LLVMCodePos) pos;
	}

	public static final long nativePtr(Op op) {
		return llvm(op).getNativePtr();
	}

	public static final long nativePtr(StructOp op) {
		return llvm(op).getNativePtr();
	}

	public static final long nativePtr(Signature<?> signature) {
		return llvm(signature).getNativePtr();
	}

	public static final long blockPtr(CodePos pos) {
		return llvm(pos).getBlockPtr();
	}


	public static final long nextPtr(Code code) {
		return llvm(code).nextPtr();
	}

	public static final long typePtr(Type<?> type) {

		final ContainerAllocation<?> allocation =
			(ContainerAllocation<?>) type.pointer(type.getGenerator())
			.getAllocation();

		return allocation.getTypePtr();
	}

	private final LLVMModule module;
	private final LLVMFunction<?> function;
	private final Code code;
	private final CodeId id;
	private LLVMCodePos.Head head;
	private LLVMCodePos.Tail tail;
	private long blockPtr;
	private int blockIdx;

	public LLVMCode(
			LLVMModule module,
			LLVMFunction<?> function,
			Code code,
			CodeId id) {
		this.module = module;
		this.code = code;
		this.id = id;
		this.function = function != null ? function : (LLVMFunction<?>) this;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	public final LLVMFunction<?> getFunction() {
		return this.function;
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	@Override
	public LLVMCodePos.Head head() {
		return this.head;
	}

	@Override
	public LLVMCodePos.Tail tail() {
		if (this.tail != null) {
			assert this.tail.getBlockPtr() == this.blockPtr :
				"Wrong tail position";
			return this.tail;
		}

		final long nextPtr;
		final long prevPtr = this.blockPtr;

		if (prevPtr != 0L) {
			endBlock();
			nextPtr = nextPtr();
			go(prevPtr, nextPtr);
		} else {
			nextPtr = nextPtr();
		}

		return this.tail;
	}

	public long nextPtr() {

		final long prevPtr = this.blockPtr;

		if (prevPtr != 0) {
			this.tail = null;
			return prevPtr;// block isn't fulfilled yet
		}

		final long nextPtr = createBlock(
				getFunction().getFunctionPtr(),
				getId().getId() + '-' + (++this.blockIdx));

		this.tail = new LLVMCodePos.Tail(this, nextPtr);

		return this.blockPtr = nextPtr;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public LLVMFunc<?> caller(CodeAllocation<?> allocation) {

		final FuncAllocation<?> alloc =
			(FuncAllocation<?>) allocation;

		return new LLVMFunc(nextPtr(), alloc.llvmId().expression(getModule()));
	}

	@Override
	public LLVMBlk block(Code code, CodeId id) {
		return new LLVMBlk(this, code, id);
	}

	@Override
	public void go(CodePos pos) {
		go(nextPtr(), blockPtr(pos));
		endBlock();
	}

	@Override
	public void go(BoolOp condition, CodePos truePos, CodePos falsePos) {

		final long blockPtr = nextPtr();
		final long truePtr;
		final long falsePtr;

		endBlock();

		final LLVMCodePos llvmTrue = llvm(truePos);
		final LLVMCodePos llvmFalse = llvm(falsePos);

		if (llvmTrue == null || llvmTrue.tailOf(this)) {
			truePtr = nextPtr();
		} else {
			truePtr = llvmTrue.getBlockPtr();
		}
		if (llvmFalse == null || llvmFalse.tailOf(this)) {
			falsePtr = nextPtr();
		} else {
			falsePtr = llvmFalse.getBlockPtr();
		}

		choose(blockPtr, nativePtr(condition), truePtr, falsePtr);
	}

	@Override
	public LLVMInt32op int32(int value) {
		return new LLVMInt32op(
				nextPtr(),
				int32(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMInt64op int64(long value) {
		return new LLVMInt64op(
				nextPtr(),
				int64(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMFp64op fp64(double value) {
		return new LLVMFp64op(
				nextPtr(),
				fp64(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMBoolOp bool(boolean value) {
		return new LLVMBoolOp(
				nextPtr(),
				bool(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMAnyOp nullPtr() {
		return new LLVMAnyOp(nextPtr(), nullPtr(getModule().getNativePtr()));
	}

	@Override
	public <O extends PtrOp> O nullPtr(DataAllocation<O> type) {

		final ContainerAllocation<O> allocation =
			(ContainerAllocation<O>) type;

		return allocation.getType().op(new LLVMStruct(
				allocation,
				nextPtr(),
				nullStructPtr(allocation.getTypePtr())));
	}

	@Override
	public <F extends Func> LLVMFunc<F> nullPtr(Signature<F> signature) {

		final LLVMSignature<F> allocation =
			(LLVMSignature<F>) signature.getAllocation();

		return new LLVMFunc<F>(
				nextPtr(),
				nullFuncPtr(allocation.getNativePtr()));
	}

	@Override
	public <O extends PtrOp> O allocateStruct(DataAllocation<O> allocation) {

		final ContainerAllocation<O> type =
			(ContainerAllocation<O>) allocation;
		final long nextPtr = nextPtr();

		return type.getType().op(new LLVMStruct(
				type,
				nextPtr,
				allocateStruct(nextPtr, type.getTypePtr())));
	}

	@Override
	public LLVMDataOp<AnyOp> allocatePtr() {

		final long nextPtr = nextPtr();

		return new LLVMDataOp.Any(nextPtr, allocatePtr(nextPtr));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O phi(O op) {

		final long nextPtr = nextPtr();

		if (op instanceof StructOp) {

			final StructOp struct = (StructOp) op;
			final LLVMStruct writer = (LLVMStruct) struct.writer();

			return (O) struct.create(writer.create(
					nextPtr,
					phi(nextPtr, writer.getBlockPtr(), writer.getNativePtr())));
		}

		final LLVMOp o = llvm(op);

		return (O) o.create(
				nextPtr,
				phi(nextPtr, o.getBlockPtr(), o.getNativePtr()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O phi(O op1, O op2) {

		final long nextPtr = nextPtr();

		if (op1 instanceof StructOp) {

			final StructOp struct1 = (StructOp) op1;
			final StructOp struct2 = (StructOp) op2;
			final LLVMStruct writer1 =
				(LLVMStruct) struct1.writer();
			final LLVMStruct writer2 =
				(LLVMStruct) struct2.writer();

			return (O) struct1.create(writer1.create(
					nextPtr,
					phi2(
							nextPtr,
							writer1.getBlockPtr(),
							writer1.getNativePtr(),
							writer2.getBlockPtr(),
							writer2.getNativePtr())));
		}

		final LLVMOp o1 = llvm(op1);
		final LLVMOp o2 = llvm(op2);

		return (O) o1.create(
				nextPtr,
				phi2(
						nextPtr,
						o1.getBlockPtr(),
						o1.getNativePtr(),
						o2.getBlockPtr(),
						o2.getNativePtr()));
	}

	@Override
	public void returnVoid() {
		this.function.getCallback().beforeReturn(this.code);
		returnVoid(nextPtr());
	}

	public void returnValue(LLVMOp result) {
		this.function.getCallback().beforeReturn(this.code);
		returnValue(nextPtr(), result.getNativePtr());
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected final void init() {
		this.blockPtr = createFirtsBlock();
		this.head = new LLVMCodePos.Head(this.blockPtr);
		this.tail = new LLVMCodePos.Tail(this, this.blockPtr);
	}

	protected abstract long createFirtsBlock();

	static native long createBlock(long functionPtr, String name);

	static native void go(long sourcePtr, long targetPtr);

	private static native void choose(
			long blockPtr,
			long conditionPtr,
			long truePtr,
			long falsePtr);


	private static native long int32(long modulePtr, int value);

	private static native long int64(long modulePtr, long value);

	private static native long fp64(long modulePtr, double value);

	private static native long bool(long modulePtr, boolean value);

	private static native long nullPtr(long modulePtr);

	private static native long nullStructPtr(long typePtr);

	private static native long nullFuncPtr(long funcTypePtr);

	private static native long allocatePtr(long blockPtr);

	private static native long allocateStruct(long blockPtr, long typePtr);

	private static native long phi(
			long blockPtr,
			long block1ptr,
			long value1ptr);

	private static native long phi2(
			long blockPtr,
			long block1ptr,
			long value1,
			long block2ptr,
			long value2);

	private static native void returnVoid(long blockPtr);

	private static native void returnValue(long blockPtr, long valuePtr);

	private void endBlock() {
		this.blockPtr = 0;
	}

}
