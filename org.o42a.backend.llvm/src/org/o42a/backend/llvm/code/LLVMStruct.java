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
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.LLVMFuncOp;
import org.o42a.backend.llvm.code.op.LLVMPtrOp;
import org.o42a.backend.llvm.code.op.LLVMRecOp;
import org.o42a.backend.llvm.data.ContainerAllocation;
import org.o42a.backend.llvm.data.LLVMDataAllocation;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public class LLVMStruct extends LLVMPtrOp implements StructWriter {

	private ContainerAllocation<?> type;

	public LLVMStruct(
			CodeId id,
			AllocClass allocClass,
			ContainerAllocation<?> type,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
		this.type = type;
	}

	public LLVMStruct(
			CodeId id,
			AllocClass allocClass,
			Type<?> type,
			long blockPtr,
			long nativePtr) {
		this(
				id,
				allocClass,
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
	public RecOp<?> field(CodeId id, Code code, Data<?> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Int8 int8(CodeId id, Code code, Int8rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int8(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Int16 int16(CodeId id, Code code, Int16rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int16(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Int32 int32(CodeId id, Code code, Int32rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int32(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Int64 int64(CodeId id, Code code, Int64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int64(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Fp32 fp32(CodeId id, Code code, Fp32rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp32(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Fp64 fp64(CodeId id, Code code, Fp64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp64(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Any ptr(CodeId id, Code code, AnyPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Data ptr(CodeId id, Code code, DataRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Data(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public <P extends StructOp> LLVMRecOp.Struct<P> ptr(
			CodeId id,
			Code code,
			StructRec<P> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Struct<P>(
				id,
				AUTO_ALLOC_CLASS,
				field.getType(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public LLVMRecOp.Rel relPtr(CodeId id, Code code, RelPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Rel(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public <O extends StructOp> O struct(CodeId id, Code code, Type<O> field) {

		final long nextPtr = nextPtr(code);

		return field.op(new LLVMStruct(
				id,
				getAllocClass(),
				field,
				nextPtr,
				field(nextPtr, id, field.pointer(code.getGenerator()))));
	}

	@Override
	public <F extends Func> FuncOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMFuncOp<F>(
				id,
				CONSTANT_ALLOC_CLASS,
				nextPtr,
				field(nextPtr, id, field),
				field.getSignature());
	}

	@Override
	public LLVMStruct create(CodeId id, long blockPtr, long nativePtr) {
		return new LLVMStruct(
				id,
				AUTO_ALLOC_CLASS,
				this.type,
				blockPtr,
				nativePtr);
	}

	@Override
	public String toString() {
		return "(" + this.type.getType().getId() + "*) " + getId();
	}

	private final long field(long blockPtr, CodeId id, Data<?> field) {
		return field(blockPtr, id, field.getPointer());
	}

	private final long field(long blockPtr, CodeId id, Ptr<?> pointer) {

		final LLVMDataAllocation<?> allocation =
			(LLVMDataAllocation<?>) pointer.getAllocation();
		final long field = field(blockPtr, id, allocation);

		assert field != 0L :
			pointer + " is not inside of " + this.type.getType();

		return field;
	}

	private long field(
			long blockPtr,
			CodeId id,
			LLVMDataAllocation<?> allocation) {

		final ContainerAllocation<?> enclosing = allocation.getEnclosing();

		if (enclosing == null) {
			return 0L;
		}
		if (enclosing.getTypePtr() == this.type.getTypePtr()) {
			return field(
					blockPtr,
					id.getId(),
					getNativePtr(),
					allocation.llvmId().getIndex());
		}

		final CodeId enclosingId =
			id.detail("enc").detail(enclosing.getType().getId());
		final long enclosingField = field(blockPtr, enclosingId, enclosing);

		if (enclosingField == 0L) {
			return 0L;
		}

		final int index = allocation.llvmId().getIndex();

		return field(
				blockPtr,
				enclosingId.anonymous(index).toString(),
				enclosingField,
				index);
	}

}
