/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.op.EndObjectUnuseFunc.END_OBJECT_UNUSE;
import static org.o42a.core.ir.op.ObjectDataFunc.OBJECT_DATA;
import static org.o42a.core.ir.op.StartObjectUseFunc.START_OBJECT_USE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Disposal;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectOp;


public final class ObjectUseOp extends IROp {

	public static final Type OBJECT_USE_TYPE = new Type();

	public static ObjectUseOp useObject(CodeId id, Code code, ObjectOp object) {

		final ObjectUseOp op =
				new ObjectUseOp(id, code.getAllocator().allocation(), object);

		op.use(code);

		return op;
	}

	private final ObjectOp object;
	private final Op ptr;

	private ObjectUseOp(CodeId id, AllocationCode code, ObjectOp object) {
		super(object.getBuilder());
		this.object = object;
		/*if (object.getPrecision().isExact()) {
			this.ptr = null;
		} else {*/
			this.ptr = code.allocate(id, OBJECT_USE_TYPE);
			ptr().objectData(null, code)
			.store(code, code.nullPtr(OBJECT_DATA_TYPE));
			code.addDisposal(new UnuseObject(this));
			getBuilder().signalGC();
		//}
	}

	public final ObjectOp object() {
		return this.object;
	}

	@Override
	public final Op ptr() {
		return this.ptr;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		if (this.ptr == null) {
			return "StaticObjectUse[" + this.object + ']';
		}
		return "ObjectUse[" + this.object + ']';
	}

	private void use(Code code) {

		final ObjectIRData.Op data = object().objectType(code).ptr().data(code);

		if (this.ptr == null) {
			getGenerator()
			.externalFunction()
			.link("o42a_obj_use_static", OBJECT_DATA)
			.op(null, code)
			.call(code, data);
			return;
		}

		getGenerator()
		.externalFunction()
		.link("o42a_obj_start_use", START_OBJECT_USE)
		.op(null, code)
		.use(code, ptr(), data);
	}

	private void unuse(Code code) {
		getGenerator()
		.externalFunction()
		.link("o42a_obj_end_use", END_OBJECT_UNUSE)
		.op(null, code)
		.unuse(code, ptr());
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRData.Op> objectData(
				CodeId id,
				Code code) {
			return ptr(id, code, getType().objectData());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRData.Op> objectData;

		private Type() {
		}

		public final StructRec<ObjectIRData.Op> objectData() {
			return this.objectData;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_obj_use_t");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.objectData = data.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

	private static final class UnuseObject implements Disposal {

		private final ObjectUseOp op;

		UnuseObject(ObjectUseOp op) {
			this.op = op;
		}

		@Override
		public void dispose(Code code) {
			this.op.unuse(code);
		}

		@Override
		public String toString() {
			if (this.op == null) {
				return super.toString();
			}
			return "Unuse[" + this.op.object() + ']';
		}

	}

}
