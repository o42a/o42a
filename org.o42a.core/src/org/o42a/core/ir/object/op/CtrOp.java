/*
    Compiler Core
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
package org.o42a.core.ir.object.op;

import static org.o42a.codegen.code.AllocationMode.LAZY_ALLOCATION;
import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.NewObjectFunc.NEW_OBJECT;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ObjectsCode;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectIRDescOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.util.string.ID;


public class CtrOp extends IROp {

	public static final Type CTR_TYPE = new Type();
	public static final Allocatable<Op> ALLOCATABLE_CTR =
			new AllocatableCtr();

	public static final ID CTR_ID = ID.id("ctr");

	private final Op ptr;

	private CtrOp(CodeBuilder builder, Op ptr) {
		super(builder);
		this.ptr = ptr;
	}

	@Override
	public final Op ptr() {
		return this.ptr;
	}

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectIRDataOp ancestorData,
			ObjectOp sample) {

		final CodeDirs subDirs = dirs.begin(
				"new_object",
				"New object: sample=" + sample);
		final Block code = subDirs.code();

		if (owner != null) {
			ptr().ownerData(code).store(code, owner.objectData(code).ptr());
		} else {
			ptr().ownerData(code).store(code, code.nullPtr(OBJECT_DATA_TYPE));
		}
		ptr().ancestorData(code).store(
				code,
				ancestorData != null
				? ancestorData : code.nullPtr(OBJECT_DATA_TYPE));
		ptr().desc(code).store(code, sample.objectData(code).loadDesc(code));

		final DataOp result = newFunc().op(null, code).newObject(code, this);

		result.isNull(null, code).go(code, subDirs.falseDir());

		final ObjectOp newObject = anonymousObject(
				sample.getBuilder(),
				result,
				sample.getWellKnownType());
		final Block resultCode = subDirs.done().code();

		return holder.holdVolatile(resultCode, newObject);
	}

	private FuncPtr<NewObjectFunc> newFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_new", NEW_OBJECT);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRDataOp> ownerData(Code code) {
			return ptr(null, code, getType().ownerData());
		}

		public final StructRecOp<ObjectIRDataOp> ancestorData(Code code) {
			return ptr(null, code, getType().ancestorData());
		}

		public final StructRecOp<ObjectIRDescOp> desc(Code code) {
			return ptr(null, code, getType().desc());
		}

		public final CtrOp op(ObjectsCode objects) {
			return new CtrOp(objects.getBuilder(), this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRDataOp> ownerData;
		private StructRec<ObjectIRDataOp> ancestorData;
		private StructRec<ObjectIRDescOp> desc;

		private Type() {
			super(ID.rawId("o42a_obj_ctr_t"));
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		public final StructRec<ObjectIRDataOp> ownerData() {
			return this.ownerData;
		}

		public final StructRec<ObjectIRDataOp> ancestorData() {
			return this.ancestorData;
		}

		public final StructRec<ObjectIRDescOp> desc() {
			return this.desc;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.ownerData = data.addPtr("owner_data", OBJECT_DATA_TYPE);
			this.ancestorData = data.addPtr("ancestor_data", OBJECT_DATA_TYPE);
			this.desc = data.addPtr("desc", OBJECT_DESC_TYPE);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0120);
		}

	}

	private static final class AllocatableCtr implements Allocatable<Op> {

		@Override
		public AllocationMode getAllocationMode() {
			return LAZY_ALLOCATION;
		}

		@Override
		public int getDisposePriority() {
			return NORMAL_DISPOSE_PRIORITY;
		}

		@Override
		public Op allocate(Allocations code, Allocated<Op> allocated) {
			return code.allocate(CTR_ID, CTR_TYPE);
		}

		@Override
		public void init(Code code, Allocated<Op> allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<Op> allocated) {
		}

	}

}
