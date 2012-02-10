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

import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.LLFAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class LLCode implements CodeWriter {

	public static final LLCode llvm(Code code) {
		return llvm(code.writer());
	}

	public static final LLBlock llvm(Block code) {
		return llvm(code.writer());
	}

	public static final LLCode llvm(CodeWriter writer) {
		return (LLCode) writer;
	}

	public static final LLBlock llvm(BlockWriter writer) {
		return (LLBlock) writer;
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
	private LLInset lastInset;

	public LLCode(LLVMModule module, LLFunction<?> function, Code code) {
		this.module = module;
		this.code = code;
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
		return this.code.getId();
	}

	public final Code code() {
		return this.code;
	}

	public abstract long nextPtr();

	public abstract long nextInstr();

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
	public final LLInset inset(Code code) {
		return this.lastInset = new LLInset(this, this.lastInset, code);
	}

	@Override
	public LLAllocation allocation(AllocationCode code) {

		final LLAllocation allocation =
				new LLAllocation(this, this.lastInset, code);

		this.lastInset = allocation;

		return allocation;
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
				instr(phi2(
						nextPtr,
						nextInstr(),
						ids.writeCodeId(id),
						ids.length(),
						o1.getBlockPtr(),
						o1.getNativePtr(),
						o2.getBlockPtr(),
						o2.getNativePtr())));
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
				instr(select(
						nextPtr,
						nextInstr(),
						ids.writeCodeId(selectId),
						ids.length(),
						condition.getNativePtr(),
						trueOp.getNativePtr(),
						falseOp.getNativePtr())));
	}

	public long instr(long instr) {
		if (this.lastInset != null) {
			this.lastInset.nextInstr(instr);
			this.lastInset = null;
		}
		return instr;
	}

	@Override
	public String toString() {
		return getId().toString();
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

	static native long stackSave(long blockPtr, long instrPtr);

	static native long stackRestore(
			long blockPtr,
			long instrPtr,
			long stackPtr);

	static native long go(
			long sourcePtr,
			long instrPtr,
			long targetPtr);

	static native long choose(
			long blockPtr,
			long instrPtr,
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

	static native long allocatePtr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen);

	static native long allocateStructPtr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long typePtr);

	static native long allocateStruct(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long typePtr);

	private static native long phi2(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long block1ptr,
			long value1,
			long block2ptr,
			long value2);

	private static native long phiN(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long[] blockAndValuePtrs);

	private static native long select(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long conditionPtr,
			long truePtr,
			long flsePtr);

	static native long returnVoid(long blockPtr, long instrPtr);

	static native long returnValue(
			long blockPtr,
			long instrPtr,
			long valuePtr);

}
