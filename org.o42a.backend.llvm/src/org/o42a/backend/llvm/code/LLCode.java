/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.code.rec.AnyRecLLOp;
import org.o42a.backend.llvm.code.rec.StructRecLLOp;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.LLFAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class LLCode implements CodeWriter {

	public static final LLCode llvm(Code code) {
		return llvm(code.writer());
	}

	public static final LLCode llvm(CodeWriter writer) {
		return (LLCode) writer;
	}

	@SuppressWarnings("unchecked")
	public static final <O extends Op> LLOp<O> llvm(O op) {
		if (op instanceof StructOp) {
			return (LLOp<O>) llvm((StructOp<?>) op);
		}
		if (op instanceof Func) {
			return (LLOp<O>) llvm((Func<?>) op);
		}
		return (LLOp<O>) op;
	}

	public static final <S extends StructOp<S>> LLStruct<S> llvm(
			StructOp<S> op) {
		return (LLStruct<S>) op.writer();
	}

	public static final <F extends Func<F>> LLFunc<F> llvm(Func<F> func) {
		return (LLFunc<F>) func.getCaller();
	}

	public static final <F extends Func<F>> LLSignature<F> llvm(
			LLVMModule module,
			Signature<F> signature) {
		return (LLSignature<F>) signature.allocation(module.getGenerator());
	}

	public static LLCodePos llvm(CodePos pos) {
		return (LLCodePos) pos;
	}

	public static final long nativePtr(Op op) {
		return llvm(op).getNativePtr();
	}

	public static final long nativePtr(StructOp<?> op) {
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

		final ContainerLLDAlloc<?> allocation =
				(ContainerLLDAlloc<?>) type.pointer(type.getGenerator())
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
	private final LLFunction<?> function;
	private final Code code;
	private final CodeId id;
	private LLCodePos.Head head;
	private LLCodePos.Tail tail;
	private long firstBlockPtr;
	private long blockPtr;
	private int blockIdx;

	public LLCode(
			LLVMModule module,
			LLFunction<?> function,
			Code code,
			CodeId id) {
		this.module = module;
		this.code = code;
		this.id = id;
		this.function = function != null ? function : (LLFunction<?>) this;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	public final LLFunction<?> getFunction() {
		return this.function;
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	@Override
	public boolean created() {
		return this.firstBlockPtr != 0L;
	}

	@Override
	public boolean exists() {
		return this.blockPtr != 0L;
	}

	@Override
	public LLCodePos.Head head() {
		return this.head;
	}

	@Override
	public LLCodePos.Tail tail() {
		if (this.tail != null) {
			assert this.tail.getBlockPtr() == getBlockPtr() :
				"Wrong tail position";
			return this.tail;
		}

		final long nextPtr;
		final long prevPtr = getBlockPtr();

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

		final long prevPtr = getBlockPtr();

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
	public <F extends Func<F>> LLFunc<F> caller(
			CodeId id,
			FuncAllocation<F> allocation) {

		final LLFAlloc<?> alloc =
				(LLFAlloc<?>) allocation;

		return new LLFunc(
				id,
				allocation.getSignature(),
				nextPtr(),
				alloc.llvmId().expression(getModule()));
	}

	@Override
	public LLBlock block(Code code, CodeId id) {
		return new LLBlock(this, code, id);
	}

	@Override
	public LLAllocation allocationBlock(AllocationCode code, CodeId id) {
		return new LLAllocation(this, code, id);
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

		final LLCodePos llvmTrue = llvm(truePos);
		final LLCodePos llvmFalse = llvm(falsePos);

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
	public Int8llOp int8(byte value) {
		return new Int8llOp(
				this.code.opId(null),
				nextPtr(),
				int8(getModule().getNativePtr(), value));
	}

	@Override
	public Int16llOp int16(short value) {
		return new Int16llOp(
				this.code.opId(null),
				nextPtr(),
				int16(getModule().getNativePtr(), value));
	}

	@Override
	public Int32llOp int32(int value) {
		return new Int32llOp(
				this.code.opId(null),
				nextPtr(),
				int32(getModule().getNativePtr(), value));
	}

	@Override
	public Int64llOp int64(long value) {
		return new Int64llOp(
				this.code.opId(null),
				nextPtr(),
				int64(getModule().getNativePtr(), value));
	}

	@Override
	public Fp32llOp fp32(float value) {
		return new Fp32llOp(
				this.code.opId(null),
				nextPtr(),
				fp32(getModule().getNativePtr(), value));
	}

	@Override
	public Fp64llOp fp64(double value) {
		return new Fp64llOp(
				this.code.opId(null),
				nextPtr(),
				fp64(getModule().getNativePtr(), value));
	}

	@Override
	public BoolLLOp bool(boolean value) {
		return new BoolLLOp(
				this.code.opId(null),
				nextPtr(),
				bool(getModule().getNativePtr(), value));
	}

	@Override
	public RelOp nullRelPtr() {
		return new RelLLOp(
				this.code.opId(null),
				nextPtr(),
				int32(getModule().getNativePtr(), 0));
	}

	@Override
	public AnyLLOp nullPtr() {
		return new AnyLLOp(
				this.code.opId(null),
				CONSTANT_ALLOC_CLASS,
				nextPtr(),
				nullPtr(getModule().getNativePtr()));
	}

	@Override
	public DataLLOp nullDataPtr() {
		return new DataLLOp(
				this.code.opId(null),
				CONSTANT_ALLOC_CLASS,
				nextPtr(),
				nullPtr(getModule().getNativePtr()));
	}

	@Override
	public <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {

		final ContainerLLDAlloc<S> allocation =
				(ContainerLLDAlloc<S>) type;

		return allocation.getType().op(new LLStruct<S>(
				this.code.opId(null),
				CONSTANT_ALLOC_CLASS,
				allocation,
				nextPtr(),
				nullStructPtr(allocation.getTypePtr())));
	}

	@Override
	public <F extends Func<F>> LLFunc<F> nullPtr(Signature<F> signature) {

		final LLSignature<F> allocation = llvm(getModule(), signature);

		return new LLFunc<F>(
				this.code.opId(null),
				signature,
				nextPtr(),
				nullFuncPtr(allocation.getNativePtr()));
	}

	@Override
	public <S extends StructOp<S>> S allocateStruct(
			CodeId id,
			DataAllocation<S> allocation) {

		final ContainerLLDAlloc<S> type =
				(ContainerLLDAlloc<S>) allocation;
		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return type.getType().op(new LLStruct<S>(
				id,
				AUTO_ALLOC_CLASS,
				type,
				nextPtr,
				allocateStruct(
						nextPtr,
						ids.writeCodeId(id),
						id.length(),
						type.getTypePtr())));
	}

	@Override
	public AnyRecLLOp allocatePtr(CodeId id) {

		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return new AnyRecLLOp(
				id,
				AUTO_ALLOC_CLASS,
				nextPtr,
				allocatePtr(nextPtr, ids.writeCodeId(id), ids.length()));
	}

	@Override
	public <S extends StructOp<S>> StructRecLLOp<S> allocatePtr(
			CodeId id,
			DataAllocation<S> allocation) {

		final ContainerLLDAlloc<S> alloc =
				(ContainerLLDAlloc<S>) allocation;
		final long nextPtr = nextPtr();
		final NativeBuffer ids = getModule().ids();

		return new StructRecLLOp<S>(
				id,
				AUTO_ALLOC_CLASS,
				alloc.getType(),
				nextPtr,
				allocateStructPtr(
						nextPtr,
						ids.writeCodeId(id),
						id.length(),
						alloc.getTypePtr()));
	}

	@Override
	public <O extends Op> O phi(CodeId id, O op) {

		final LLOp<O> o = llvm(op);

		return o.create(id, nextPtr(), o.getNativePtr());
	}

	@Override
	public <O extends Op> O phi(CodeId id, O op1, O op2) {

		final long nextPtr = nextPtr();
		final LLOp<O> o1 = llvm(op1);
		final LLOp<O> o2 = llvm(op2);
		final NativeBuffer ids = getModule().ids();

		return o1.create(
				id,
				nextPtr,
				phi2(
						nextPtr,
						ids.writeCodeId(id),
						ids.length(),
						o1.getBlockPtr(),
						o1.getNativePtr(),
						o2.getBlockPtr(),
						o2.getNativePtr()));
	}

	public <O extends Op> O select(
			CodeId id,
			BoolLLOp condition,
			O trueValue,
			O falseValue) {

		final LLOp<O> trueOp = llvm(trueValue);
		final LLOp<O> falseOp = llvm(falseValue);
		final long nextPtr = nextPtr();
		final CodeId selectId =
				id != null ? id : condition.getId().sub("select");
		final NativeBuffer ids = getModule().ids();

		return trueOp.create(
				id,
				nextPtr,
				select(
						nextPtr,
						ids.writeCodeId(selectId),
						ids.length(),
						condition.getNativePtr(),
						trueOp.getNativePtr(),
						falseOp.getNativePtr()));
	}

	@Override
	public void returnVoid() {
		this.function.getCallback().beforeReturn(this.code);
		returnVoid(nextPtr());
	}

	public void returnValue(LLOp<?> result) {
		this.function.getCallback().beforeReturn(this.code);
		returnValue(nextPtr(), result.getNativePtr());
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected final void init() {
		this.head = new LLCodePos.Head(this);
		this.tail = new LLCodePos.Tail(this);
	}

	protected abstract long createFirtsBlock();

	final long getFirstBlockPtr() {
		if (created()) {
			return this.firstBlockPtr;
		}
		return this.firstBlockPtr = this.blockPtr = createFirtsBlock();
	}

	final long getBlockPtr() {
		if (created()) {
			return this.blockPtr;
		}
		return getFirstBlockPtr();
	}

	private long createNextBlock() {
		return createBlock(getFunction(), getId().anonymous(++this.blockIdx));
	}

	private long setNextPtr(final long nextPtr) {
		this.tail = new LLCodePos.Tail(this, nextPtr);
		return this.blockPtr = nextPtr;
	}

	private final void endBlock() {
		this.blockPtr = 0;
		this.tail = null;
	}

	static long createBlock(LLFunction<?> function, CodeId id) {

		final NativeBuffer ids = function.getModule().ids();

		return createBlock(
				function.getFunctionPtr(),
				ids.writeCodeId(id),
				ids.length());
	}

	private static native long createBlock(
			long functionPtr,
			long id,
			int idLen);

	static native long stackSave(long blockPtr);

	static native void stackRestore(long blockPtr, long stackPtr);

	static native void go(long sourcePtr, long targetPtr);

	private static native void choose(
			long blockPtr,
			long conditionPtr,
			long truePtr,
			long falsePtr);

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
			long id,
			int idLen);

	private static native long allocateStructPtr(
			long blockPtr,
			long id,
			int idLen,
			long typePtr);

	private static native long allocateStruct(
			long blockPtr,
			long id,
			int idLen,
			long typePtr);

	private static native long phi2(
			long blockPtr,
			long id,
			int idLen,
			long block1ptr,
			long value1,
			long block2ptr,
			long value2);

	private static native long phiN(
			long blockPtr,
			long id,
			int idLen,
			long[] blockAndValuePtrs);

	private static native long select(
			long blockPtr,
			long id,
			int idLen,
			long conditionPtr,
			long truePtr,
			long flsePtr);

	private static native void returnVoid(long blockPtr);

	private static native void returnValue(long blockPtr, long valuePtr);

}
