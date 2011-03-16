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

import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.backend.llvm.code.op.LLVMCodeOp;
import org.o42a.backend.llvm.code.op.LLVMPtrOp;
import org.o42a.backend.llvm.code.op.LLVMRecOp;
import org.o42a.backend.llvm.data.ContainerAllocation;
import org.o42a.backend.llvm.data.LLVMDataAllocation;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public class LLVMStruct extends LLVMPtrOp implements StructWriter {

	private ContainerAllocation<?> type;

	public LLVMStruct(
			ContainerAllocation<?> type,
			long blockPtr,
			long nativePtr) {
		super(blockPtr, nativePtr);
		this.type = type;
	}

	public LLVMStruct(
			Type<?> type,
			long blockPtr,
			long nativePtr) {
		this(
				(ContainerAllocation<?>) type.pointer(type.getGenerator())
				.getAllocation(),
				blockPtr,
				nativePtr);
	}

	@Override
	public Type<? extends StructOp> getType() {
		return this.type.getType();
	}

	@Override
	public RecOp<?> field(Code code, Data<?> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(nextPtr, field(nextPtr, field));
	}

	@Override
	public RecOp<Int32op> int32(Code code, Int32rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int32(nextPtr, field(nextPtr, field));
	}

	@Override
	public RecOp<Int64op> int64(Code code, Int64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int64(nextPtr, field(nextPtr, field));
	}

	@Override
	public RecOp<Fp64op> fp64(Code code, Fp64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp64(nextPtr, field(nextPtr, field));
	}

	@Override
	public RecOp<AnyOp> ptr(Code code, AnyPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(nextPtr, field(nextPtr, field));
	}

	@Override
	public RecOp<DataOp> ptr(Code code, DataRec<?> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Data(nextPtr, field(nextPtr, field));
	}

	@Override
	public <P extends StructOp> RecOp<P> ptr(
			Code code,
			StructRec<P> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Struct<P>(
				field.getType(),
				nextPtr,
				field(nextPtr, field));
	}

	@Override
	public RecOp<RelOp> relPtr(Code code, RelPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Rel(nextPtr, field(nextPtr, field));
	}

	@Override
	public <O extends StructOp> O struct(Code code, Type<O> field) {

		final long nextPtr = nextPtr(code);

		return field.op(new LLVMStruct(
				field,
				nextPtr,
				field(nextPtr, field.pointer(code.getGenerator()))));
	}

	@Override
	public <F extends Func> FuncOp<F> func(Code code, FuncRec<F> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMCodeOp<F>(
				nextPtr,
				field(nextPtr, field),
				field.getSignature());
	}

	@Override
	public LLVMStruct create(long blockPtr, long nativePtr) {
		return new LLVMStruct(this.type, blockPtr, nativePtr);
	}

	@Override
	public CodeBackend backend() {
		return this.type.getModule().codeBackend();
	}

	private final long field(long blockPtr, Data<?> field) {
		return field(blockPtr, field.getPointer());
	}

	private final long field(long blockPtr, Ptr<?> pointer) {

		final LLVMDataAllocation<?> allocation =
			(LLVMDataAllocation<?>) pointer.getAllocation();
		final long field = field(blockPtr, allocation);

		assert field != 0L :
			pointer + " is not inside of " + this.type.getType();

		return field;
	}

	private long field(long blockPtr, LLVMDataAllocation<?> allocation) {

		final ContainerAllocation<?> enclosing = allocation.getEnclosing();

		if (enclosing == null) {
			return 0L;
		}
		if (enclosing.getTypePtr() == this.type.getTypePtr()) {
			return field(
					blockPtr,
					getNativePtr(),
					allocation.llvmId().getIndex());
		}

		final long enclosingField = field(blockPtr, enclosing);

		if (enclosingField == 0L) {
			return 0L;
		}

		return field(blockPtr, enclosingField, allocation.llvmId().getIndex());
	}

}
