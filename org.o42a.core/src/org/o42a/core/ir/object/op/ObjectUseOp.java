/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.op.EndObjectUnuseFunc.END_OBJECT_UNUSE;
import static org.o42a.core.ir.object.op.StartObjectUseFunc.START_OBJECT_USE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.IROp;
import org.o42a.util.string.ID;


public final class ObjectUseOp extends IROp {

	public static final Type OBJECT_USE_TYPE = new Type();
	private static final AllocatableObjectUse ALLOCATABLE_OBJECT_USE =
			new AllocatableObjectUse();

	private final Allocated<AllocatedObjectUse> ptr;

	ObjectUseOp(ID id, CodeBuilder builder, Allocator allocator) {
		super(builder);
		this.ptr = allocator.allocate(id, ALLOCATABLE_OBJECT_USE);
		getBuilder().gc().signal();
	}

	@Override
	public final Op ptr() {
		return this.ptr.get().op;
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return "ObjectUse[" + this.ptr + ']';
	}

	void setUsed(Code code, ObjectOp object) {
		code.dumpName("Trapped object caught: ", object);

		final ObjectIRDataOp data = object.objectData(code).ptr();

		this.ptr.get().objectData.store(code, data);
	}

	void startUse(Code code, ObjectOp object) {

		final ObjectIRDataOp data = object.objectData(code).ptr();

		getGenerator()
		.externalFunction()
		.link("o42a_obj_start_use", START_OBJECT_USE)
		.op(null, code)
		.use(code, ptr(), data);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRDataOp> objectData(ID id, Code code) {
			return ptr(id, code, getType().objectData());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRDataOp> objectData;

		private Type() {
			super(ID.rawId("o42a_obj_use_t"));
		}

		public final StructRec<ObjectIRDataOp> objectData() {
			return this.objectData;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.objectData = data.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

	private static final class AllocatedObjectUse {

		private final Op op;
		private StructRecOp<ObjectIRDataOp> objectData;

		AllocatedObjectUse(Op op) {
			this.op = op;
		}

		@Override
		public String toString() {
			if (this.op == null) {
				return super.toString();
			}
			return toString();
		}

		void init(Code code) {
			this.objectData = this.op.objectData(null, code);
			this.objectData.store(code, code.nullPtr(OBJECT_DATA_TYPE));
		}

	}

	private static final class AllocatableObjectUse
			implements Allocatable<AllocatedObjectUse> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public int getPriority() {
			return NORMAL_ALLOC_PRIORITY;
		}

		@Override
		public AllocatedObjectUse allocate(
				Allocations code,
				Allocated<AllocatedObjectUse> allocated) {
			return new AllocatedObjectUse(code.allocate(OBJECT_USE_TYPE));
		}

		@Override
		public void init(Code code, Allocated<AllocatedObjectUse> allocated) {
			allocated.get().init(code);
		}

		@Override
		public void dispose(
				Code code,
				Allocated<AllocatedObjectUse> allocated) {
			code.getGenerator()
			.externalFunction()
			.link("o42a_obj_end_use", END_OBJECT_UNUSE)
			.op(null, code)
			.unuse(code, allocated.get().op);
		}

		@Override
		public String toString() {
			return "ObjectUse";
		}

	}

}
