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
import org.o42a.backend.llvm.code.op.LLVMDataOp;
import org.o42a.backend.llvm.code.op.LLVMPtrOp;
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
	public Type<?> getType() {
		return this.type.getType();
	}

	@Override
	public DataOp<Int32op> int32(Code code, Int32rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Int32(nextPtr, field(nextPtr, field));
	}

	@Override
	public DataOp<Int64op> int64(Code code, Int64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Int64(nextPtr, field(nextPtr, field));
	}

	@Override
	public DataOp<Fp64op> fp64(Code code, Fp64rec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Fp64(nextPtr, field(nextPtr, field));
	}

	@Override
	public DataOp<AnyOp> ptr(Code code, AnyPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Any(nextPtr, field(nextPtr, field));
	}

	@Override
	public <P extends StructOp> DataOp<P> ptr(
			Code code,
			StructPtrRec<P> field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Struct<P>(
				field.getType(),
				nextPtr,
				field(nextPtr, field));
	}

	@Override
	public DataOp<RelOp> relPtr(Code code, RelPtrRec field) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Rel(nextPtr, field(nextPtr, field));
	}

	@Override
	public <O extends PtrOp> O struct(Code code, Type<O> field) {

		final long nextPtr = nextPtr(code);

		return field.op(new LLVMStruct(
				field,
				nextPtr,
				field(nextPtr, field.pointer(code.getGenerator()))));
	}

	@Override
	public <F extends Func> CodeOp<F> func(Code code, CodeRec<F> field) {

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
		// Rely on LLVM ability to unify types with the same structure.
		// So, despite getTypePtr() (pointing to PATypeHolder) can be different,
		// the getUniquePtr() (pointing to Type) are the same for the similar
		// structures.
		// This may happen e.g. for object bodies of the same ascendant
		// in different objects.
		if (enclosing.getUniqueTypePtr() == this.type.getUniqueTypePtr()) {
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
