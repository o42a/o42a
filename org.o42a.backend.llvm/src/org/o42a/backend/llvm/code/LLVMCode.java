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
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.backend.FuncAllocation;
import org.o42a.codegen.code.backend.MultiCodePos;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class LLVMCode implements CodeWriter {

	public static final LLVMCode llvm(Code code) {
		return llvm(code.writer());
	}

	public static final LLVMCode llvm(CodeWriter writer) {
		return (LLVMCode) writer;
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
		return (LLVMStruct) op.getWriter();
	}

	public static final LLVMFunc<?> llvm(Func func) {
		return (LLVMFunc<?>) func.getCaller();
	}

	public static final <F extends Func> LLVMSignature<F> llvm(
			LLVMModule module,
			Signature<F> signature) {
		return (LLVMSignature<F>) signature.allocation(module.getGenerator());
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

	public static final long blockPtr(CodePos pos) {
		return llvm(pos).getBlockPtr();
	}

	public static final long nextPtr(Code code) {
		return llvm(code).nextPtr();
	}

	public static final long nextPtr(CodeWriter writer) {
		return llvm(writer).nextPtr();
	}

	public static final long typePtr(Type<?> type) {

		final ContainerAllocation<?> allocation =
			(ContainerAllocation<?>) type.pointer(type.getGenerator())
			.getAllocation();

		return allocation.getTypePtr();
	}

	public static CodeId castId(Op op, CodeId id, Code code, String suffix) {
		if (id != null) {
			return code.opId(id);
		}
		return op.getId().type(code.id(suffix));
	}

	public static CodeId castId(Op op, CodeId id, Code code, CodeId suffix) {
		if (id != null) {
			return code.opId(id);
		}
		return op.getId().type(suffix);
	}

	public static CodeId unaryId(
			Op op,
			CodeId id,
			Code code,
			String operator) {
		if (id != null) {
			return code.opId(id);
		}
		return code.getGenerator().id(operator).detail(op.getId());
	}

	public static CodeId binaryId(
			Op left,
			CodeId id,
			Code code,
			String operator,
			Op right) {
		if (id != null) {
			return code.opId(id);
		}
		return left.getId().detail(operator).detail(right.getId());
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

		return setNextPtr(createNextBlock());
	}

	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	@Override
	public <F extends Func> LLVMFunc<F> caller(
			CodeId id,
			FuncAllocation<F> allocation) {

		final LLVMFuncAllocation<?> alloc =
			(LLVMFuncAllocation<?>) allocation;

		return new LLVMFunc(
				id,
				allocation.getSignature(),
				nextPtr(),
				alloc.llvmId().expression(getModule()));
	}

	@Override
	public LLVMBlock block(Code code, CodeId id) {
		return new LLVMBlock(this, code, id);
	}

	@Override
	public LLVMAllocationBlock allocationBlock(Code code, CodeId id) {
		return new LLVMAllocationBlock(this, code, id);
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
	public MultiCodePos comeFrom(CodeWriter[] alts) {

		final long targetBlockPtrs[] = new long[alts.length];
		final long blocksAndAddrs[] = new long[alts.length << 1];
		int j = 0;

		for (int i = 0; i < alts.length; ++i) {

			final LLVMCode llvmAlt = llvm(alts[i]);
			final long blockPtr = llvmAlt.nextPtr();
			final long nextPtr = llvmAlt.createNextBlock();

			blocksAndAddrs[j++] = blockPtr;
			blocksAndAddrs[j++] = blockAddress(blockPtr, nextPtr);

			go(blockPtr, nextPtr());
			llvmAlt.setNextPtr(nextPtr);

			targetBlockPtrs[i] = nextPtr;
		}

		final long targetPtr =
			phiN(nextPtr(), this.code.opId(null).getId(), blocksAndAddrs);

		return new LLVMMultiCodePos(targetPtr, targetBlockPtrs);
	}

	@Override
	public void goToOneOf(MultiCodePos target) {

		final LLVMMultiCodePos llvmTarget = (LLVMMultiCodePos) target;

		indirectbr(
				nextPtr(),
				llvmTarget.getTargetPtr(),
				llvmTarget.getTargetBlockPtrs());
	}

	@Override
	public LLVMInt8op int8(byte value) {
		return new LLVMInt8op(
				this.code.opId(null),
				nextPtr(),
				int8(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMInt16op int16(short value) {
		return new LLVMInt16op(
				this.code.opId(null),
				nextPtr(),
				int16(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMInt32op int32(int value) {
		return new LLVMInt32op(
				this.code.opId(null),
				nextPtr(),
				int32(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMInt64op int64(long value) {
		return new LLVMInt64op(
				this.code.opId(null),
				nextPtr(),
				int64(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMFp32op fp32(float value) {
		return new LLVMFp32op(
				this.code.opId(null),
				nextPtr(),
				fp32(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMFp64op fp64(double value) {
		return new LLVMFp64op(
				this.code.opId(null),
				nextPtr(),
				fp64(getModule().getNativePtr(), value));
	}

	@Override
	public LLVMBoolOp bool(boolean value) {
		return new LLVMBoolOp(
				this.code.opId(null),
				nextPtr(),
				bool(getModule().getNativePtr(), value));
	}

	@Override
	public RelOp nullRelPtr() {
		return new LLVMRelOp(
				this.code.opId(null),
				nextPtr(),
				int32(getModule().getNativePtr(), 0));
	}

	@Override
	public LLVMAnyOp nullPtr() {
		return new LLVMAnyOp(
				this.code.opId(null),
				nextPtr(),
				nullPtr(getModule().getNativePtr()));
	}

	@Override
	public LLVMDataOp nullDataPtr() {
		return new LLVMDataOp(
				this.code.opId(null),
				nextPtr(),
				nullPtr(getModule().getNativePtr()));
	}

	@Override
	public <O extends StructOp> O nullPtr(DataAllocation<O> type) {

		final ContainerAllocation<O> allocation =
			(ContainerAllocation<O>) type;

		return allocation.getType().op(new LLVMStruct(
				this.code.opId(null),
				allocation,
				nextPtr(),
				nullStructPtr(allocation.getTypePtr())));
	}

	@Override
	public <F extends Func> LLVMFunc<F> nullPtr(Signature<F> signature) {

		final LLVMSignature<F> allocation = llvm(getModule(), signature);

		return new LLVMFunc<F>(
				this.code.opId(null),
				signature,
				nextPtr(),
				nullFuncPtr(allocation.getNativePtr()));
	}

	@Override
	public <O extends StructOp> O allocateStruct(
			CodeId id,
			DataAllocation<O> allocation) {

		final ContainerAllocation<O> type =
			(ContainerAllocation<O>) allocation;
		final long nextPtr = nextPtr();

		return type.getType().op(new LLVMStruct(
				id,
				type,
				nextPtr,
				allocateStruct(nextPtr, id.getId(), type.getTypePtr())));
	}

	@Override
	public LLVMRecOp<AnyOp> allocatePtr(CodeId id) {

		final long nextPtr = nextPtr();

		return new LLVMRecOp.Any(
				id,
				nextPtr,
				allocatePtr(nextPtr, id.toString()));
	}

	@Override
	public <O extends StructOp> RecOp<O> allocatePtr(
			CodeId id,
			DataAllocation<O> allocation) {

		final ContainerAllocation<O> alloc =
			(ContainerAllocation<O>) allocation;
		final long nextPtr = nextPtr();

		return new LLVMRecOp.Struct<O>(
				id,
				alloc.getType(),
				nextPtr,
				allocateStructPtr(nextPtr, id.getId(), alloc.getTypePtr()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O phi(CodeId id, O op) {

		final long nextPtr = nextPtr();

		if (op instanceof StructOp) {

			final StructOp struct = (StructOp) op;
			final LLVMStruct writer = llvm(struct);

			return (O) struct.getType().op(writer.create(
					id,
					nextPtr,
					phi(
							nextPtr,
							id.getId(),
							writer.getBlockPtr(),
							writer.getNativePtr())));
		}

		final LLVMOp o = llvm(op);

		return (O) o.create(
				id,
				nextPtr,
				phi(nextPtr, id.getId(), o.getBlockPtr(), o.getNativePtr()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O phi(CodeId id, O op1, O op2) {

		final long nextPtr = nextPtr();

		if (op1 instanceof StructOp) {

			final StructOp struct1 = (StructOp) op1;
			final StructOp struct2 = (StructOp) op2;
			final LLVMStruct writer1 = llvm(struct1);
			final LLVMStruct writer2 = llvm(struct2);

			return (O) struct1.getType().op(writer1.create(
					id,
					nextPtr,
					phi2(
							nextPtr,
							id.getId(),
							writer1.getBlockPtr(),
							writer1.getNativePtr(),
							writer2.getBlockPtr(),
							writer2.getNativePtr())));
		}

		final LLVMOp o1 = llvm(op1);
		final LLVMOp o2 = llvm(op2);

		return (O) o1.create(
				id,
				nextPtr,
				phi2(
						nextPtr,
						id.getId(),
						o1.getBlockPtr(),
						o1.getNativePtr(),
						o2.getBlockPtr(),
						o2.getNativePtr()));
	}

	public <O extends Op> O select(
			CodeId id,
			LLVMBoolOp condition,
			O trueValue,
			O falseValue) {

		final long nextPtr = nextPtr();

		return create(trueValue, id, nextPtr, select(
				nextPtr,
				(id != null ? id : condition.getId().sub("select")).getId(),
				condition.getNativePtr(),
				nativePtr(trueValue),
				nativePtr(falseValue)));
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
		this.head = new LLVMCodePos.Head(this, this.blockPtr);
		this.tail = new LLVMCodePos.Tail(this, this.blockPtr);
	}

	protected abstract long createFirtsBlock();

	private long createNextBlock() {
		return createBlock(
				getFunction().getFunctionPtr(),
				getId().getId() + '-' + (++this.blockIdx));
	}

	private long setNextPtr(final long nextPtr) {
		this.tail = new LLVMCodePos.Tail(this, nextPtr);
		return this.blockPtr = nextPtr;
	}

	@SuppressWarnings("unchecked")
	private static <O extends Op> O create(
			O sample,
			CodeId id,
			long blockPtr,
			long nativePtr) {

		if (sample instanceof StructOp) {

			final StructOp struct = (StructOp) sample;
			final LLVMStruct writer = llvm(struct);

			return (O) struct.getType().op(
					writer.create(id, blockPtr, nativePtr));
		}

		return (O) llvm(sample).create(id, blockPtr, nativePtr);
	}

	static native long createBlock(long functionPtr, String name);

	static native long stackSave(long blockPtr);

	static native void stackRestore(long blockPtr, long stackPtr);

	static native void go(long sourcePtr, long targetPtr);

	private static native void choose(
			long blockPtr,
			long conditionPtr,
			long truePtr,
			long falsePtr);

	private static native long blockAddress(long blockPtr, long targetPtr);

	private static native long indirectbr(
			long blockPtr,
			long targetPtr,
			long[] targetBlockPtrs);

	private static native long int8(long modulePtr, byte value);

	private static native long int16(long modulePtr, short value);

	private static native long int32(long modulePtr, int value);

	private static native long int64(long modulePtr, long value);

	private static native long fp32(long modulePtr, float value);

	private static native long fp64(long modulePtr, double value);

	private static native long bool(long modulePtr, boolean value);

	private static native long nullPtr(long modulePtr);

	private static native long nullStructPtr(long typePtr);

	private static native long nullFuncPtr(long funcTypePtr);

	private static native long allocatePtr(
			long blockPtr,
			String id);

	private static native long allocateStructPtr(
			long blockPtr,
			String id,
			long typePtr);

	private static native long allocateStruct(
			long blockPtr,
			String id,
			long typePtr);

	private static native long phi(
			long blockPtr,
			String id,
			long block1ptr,
			long value1ptr);

	private static native long phi2(
			long blockPtr,
			String id,
			long block1ptr,
			long value1,
			long block2ptr,
			long value2);

	private static native long phiN(
			long blockPtr,
			String id,
			long[] blockAndValuePtrs);

	private static native long select(
			long blockPtr,
			String id,
			long conditionPtr,
			long truePtr,
			long flsePtr);

	private static native void returnVoid(long blockPtr);

	private static native void returnValue(long blockPtr, long valuePtr);

	private void endBlock() {
		this.blockPtr = 0;
	}

}
