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

import static org.o42a.backend.llvm.code.LLCode.nextPtr;
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.FuncLLOp;
import org.o42a.backend.llvm.code.op.PtrLLOp;
import org.o42a.backend.llvm.code.rec.*;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.LLDAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public class LLStruct<S extends StructOp<S>>
		extends PtrLLOp<S>
		implements StructWriter<S> {

	private ContainerLLDAlloc<S> type;

	public LLStruct(
			CodeId id,
			AllocClass allocClass,
			ContainerLLDAlloc<S> type,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
		this.type = type;
	}

	public LLStruct(
			CodeId id,
			AllocClass allocClass,
			Type<S> type,
			long blockPtr,
			long nativePtr) {
		this(
				id,
				allocClass,
				(ContainerLLDAlloc<S>) type.pointer(type.getGenerator())
				.getAllocation(),
				blockPtr,
				nativePtr);
	}

	@Override
	public Type<S> getType() {
		return this.type.getType();
	}

	@Override
	public AnyRecOp field(CodeId id, Code code, Data<?> field) {

		final long nextPtr = nextPtr(code);

		return new AnyRecLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Int8recLLOp int8(CodeId id, Code code, Int8rec field) {

		final long nextPtr = nextPtr(code);

		return new Int8recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Int16recLLOp int16(CodeId id, Code code, Int16rec field) {

		final long nextPtr = nextPtr(code);

		return new Int16recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Int32recLLOp int32(CodeId id, Code code, Int32rec field) {

		final long nextPtr = nextPtr(code);

		return new Int32recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Int64recLLOp int64(CodeId id, Code code, Int64rec field) {

		final long nextPtr = nextPtr(code);

		return new Int64recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Fp32recLLOp fp32(CodeId id, Code code, Fp32rec field) {

		final long nextPtr = nextPtr(code);

		return new Fp32recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public Fp64recLLOp fp64(CodeId id, Code code, Fp64rec field) {

		final long nextPtr = nextPtr(code);

		return new Fp64recLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public AnyRecLLOp ptr(CodeId id, Code code, AnyRec field) {

		final long nextPtr = nextPtr(code);

		return new AnyRecLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public DataRecLLOp ptr(CodeId id, Code code, DataRec field) {

		final long nextPtr = nextPtr(code);

		return new DataRecLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public <SS extends StructOp<SS>> StructRecLLOp<SS> ptr(
			CodeId id,
			Code code,
			StructRec<SS> field) {

		final long nextPtr = nextPtr(code);

		return new StructRecLLOp<SS>(
				id,
				AUTO_ALLOC_CLASS,
				field.getType(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public RelRecLLOp relPtr(CodeId id, Code code, RelRec field) {

		final long nextPtr = nextPtr(code);

		return new RelRecLLOp(
				id,
				getAllocClass(),
				nextPtr,
				field(nextPtr, id, field));
	}

	@Override
	public <SS extends StructOp<SS>> SS struct(
			CodeId id,
			Code code,
			Type<SS> field) {

		final long nextPtr = nextPtr(code);

		return field.op(new LLStruct<SS>(
				id,
				getAllocClass(),
				field,
				nextPtr,
				field(nextPtr, id, field.pointer(code.getGenerator()))));
	}

	@Override
	public <F extends Func<F>> FuncOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {

		final long nextPtr = nextPtr(code);

		return new FuncLLOp<F>(
				id,
				CONSTANT_ALLOC_CLASS,
				nextPtr,
				field(nextPtr, id, field),
				field.getSignature());
	}

	@Override
	public S create(CodeId id, long blockPtr, long nativePtr) {
		return getType().op(new LLStruct<S>(
				id,
				AUTO_ALLOC_CLASS,
				this.type,
				blockPtr,
				nativePtr));
	}

	@Override
	public String toString() {
		return "(" + this.type.getType().getId() + "*) " + getId();
	}

	private final long field(long blockPtr, CodeId id, Data<?> field) {
		return field(blockPtr, id, field.getPointer());
	}

	private final long field(long blockPtr, CodeId id, Ptr<?> pointer) {

		final LLDAlloc<?> allocation =
				(LLDAlloc<?>) pointer.getAllocation();
		final long field = field(blockPtr, id, allocation);

		assert field != 0L :
			pointer + " is not inside of " + this.type.getType();

		return field;
	}

	private long field(
			long blockPtr,
			CodeId id,
			LLDAlloc<?> allocation) {

		final ContainerLLDAlloc<?> enclosing = allocation.getEnclosing();

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
