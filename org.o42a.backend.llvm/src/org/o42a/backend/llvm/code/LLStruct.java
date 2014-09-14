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
package org.o42a.backend.llvm.code;

import static org.o42a.backend.llvm.code.LLCode.llvm;

import org.o42a.backend.llvm.code.op.DataPtrLLOp;
import org.o42a.backend.llvm.code.op.FuncLLOp;
import org.o42a.backend.llvm.code.op.SystemLLOp;
import org.o42a.backend.llvm.code.rec.*;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.LLDAlloc;
import org.o42a.backend.llvm.data.alloc.SystemTypeLLAlloc;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public class LLStruct<S extends StructOp<S>>
		extends DataPtrLLOp<S>
		implements StructWriter<S> {

	private static final String ENCLOSING_ID = "enc";

	private ContainerLLDAlloc<S> type;

	public LLStruct(
			ID id,
			AllocPlace allocPlace,
			ContainerLLDAlloc<S> type,
			long blockPtr,
			long nativePtr) {
		super(id, allocPlace, blockPtr, nativePtr);
		this.type = type;
	}

	public LLStruct(
			ID id,
			AllocPlace allocPlace,
			Type<S> type,
			long blockPtr,
			long nativePtr) {
		this(
				id,
				allocPlace,
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
	public Int8recLLOp int8(ID id, Code code, Int8rec field) {

		final LLCode llvm = llvm(code);

		return new Int8recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public Int16recLLOp int16(ID id, Code code, Int16rec field) {

		final LLCode llvm = llvm(code);

		return new Int16recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public Int32recLLOp int32(ID id, Code code, Int32rec field) {

		final LLCode llvm = llvm(code);

		return new Int32recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public Int64recLLOp int64(ID id, Code code, Int64rec field) {

		final LLCode llvm = llvm(code);

		return new Int64recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public Fp32recLLOp fp32(ID id, Code code, Fp32rec field) {

		final LLCode llvm = llvm(code);

		return new Fp32recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public Fp64recLLOp fp64(ID id, Code code, Fp64rec field) {

		final LLCode llvm = llvm(code);

		return new Fp64recLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public SystemOp system(ID id, Code code, SystemData field) {

		final LLCode llvm = llvm(code);
		final SystemTypeLLAlloc typeAlloc =
				(SystemTypeLLAlloc) field.getSystemType().getAllocation();

		return new SystemLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				typeAlloc.exists() ? field(id, llvm, field) : 0L,
				typeAlloc);
	}

	@Override
	public AnyRecLLOp ptr(ID id, Code code, AnyRec field) {

		final LLCode llvm = llvm(code);

		return new AnyRecLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public DataRecLLOp ptr(ID id, Code code, DataRec field) {

		final LLCode llvm = llvm(code);

		return new DataRecLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public <SS extends StructOp<SS>> StructRecLLOp<SS> ptr(
			ID id,
			Code code,
			StructRec<SS> field) {

		final LLCode llvm = llvm(code);

		return new StructRecLLOp<>(
				id,
				null,
				field.getType(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public RelRecLLOp relPtr(ID id, Code code, RelRec field) {

		final LLCode llvm = llvm(code);

		return new RelRecLLOp(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field));
	}

	@Override
	public <SS extends StructOp<SS>> SS struct(
			ID id,
			Code code,
			Type<SS> field) {

		final LLCode llvm = llvm(code);

		return field.op(new LLStruct<>(
				id,
				getAllocPlace(),
				field,
				llvm.nextPtr(),
				field(id, llvm, field.pointer(code.getGenerator()))));
	}

	@Override
	public <F extends Fn<F>> FuncOp<F> func(
			ID id,
			Code code,
			FuncRec<F> field) {

		final LLCode llvm = llvm(code);

		return new FuncLLOp<>(
				id,
				getAllocPlace(),
				llvm.nextPtr(),
				field(id, llvm, field),
				field.getSignature());
	}

	@Override
	public S create(ID id, long blockPtr, long nativePtr) {
		return getType().op(new LLStruct<>(
				id,
				null,
				this.type,
				blockPtr,
				nativePtr));
	}

	@Override
	public String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return "(" + this.type.getType().getId() + "*) " + getId();
	}

	private final long field(ID id, LLCode code, Data<?> field) {
		return field(id, code, field.getPointer());
	}

	private final long field(ID id, LLCode code, Ptr<?> pointer) {

		final LLDAlloc<?> allocation =
				(LLDAlloc<?>) pointer.getAllocation();
		final long field = field(id, code, allocation);

		assert field != 0L :
			pointer + " is not inside of " + this.type.getType();

		return field;
	}

	private long field(ID id, LLCode code, LLDAlloc<?> allocation) {

		final ContainerLLDAlloc<?> enclosing = allocation.getEnclosing();

		if (enclosing == null) {
			return 0L;
		}

		final NativeBuffer ids = code.getModule().ids();

		if (enclosing.getTypePtr() == this.type.getTypePtr()) {
			return code.instr(field(
					code.nextPtr(),
					code.nextInstr(),
					ids.write(id),
					ids.length(),
					getNativePtr(),
					allocation.llvmId().getIndex()));
		}

		final ID enclosingId =
				id.detail(ENCLOSING_ID).detail(enclosing.getType().getId());
		final long enclosingField =
				field(enclosingId, code, enclosing);

		if (enclosingField == 0L) {
			return 0L;
		}

		final int index = allocation.llvmId().getIndex();

		return code.instr(field(
				code.nextPtr(),
				code.nextInstr(),
				ids.write(enclosingId.anonymous(index)),
				ids.length(),
				enclosingField,
				index));
	}

}
