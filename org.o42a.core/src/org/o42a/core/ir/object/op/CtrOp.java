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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.NewObjectFunc.NEW_OBJECT;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ObjectsCode;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.util.string.ID;


public class CtrOp extends IROp {

	public static final Type CTR_TYPE = new Type();

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
			ObjectIRTypeOp ancestorType,
			ObjectOp sample) {

		final CodeDirs subDirs = dirs.begin(
				"new_object",
				"New object: sample=" + sample);
		final Block code = subDirs.code();

		if (owner != null) {
			ptr().ownerType(code).store(code, owner.objectType(code).ptr());
		} else {
			ptr().ownerType(code).store(code, code.nullPtr(OBJECT_TYPE));
		}
		ptr().ancestorType(code).store(
				code,
				ancestorType != null
				? ancestorType : code.nullPtr(OBJECT_TYPE));
		ptr().type(code).store(code, sample.objectType(code).ptr());

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

		public final StructRecOp<ObjectIRTypeOp> ownerType(Code code) {
			return ptr(null, code, getType().ownerType());
		}

		public final StructRecOp<ObjectIRTypeOp> ancestorType(Code code) {
			return ptr(null, code, getType().ancestorType());
		}

		public final StructRecOp<ObjectIRTypeOp> type(Code code) {
			return ptr(null, code, getType().type());
		}

		public final CtrOp op(ObjectsCode objects) {
			return new CtrOp(objects.getBuilder(), this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRTypeOp> ownerType;
		private StructRec<ObjectIRTypeOp> ancestorType;
		private StructRec<ObjectIRTypeOp> type;

		private Type() {
			super(ID.rawId("o42a_obj_ctr_t"));
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		public final StructRec<ObjectIRTypeOp> ownerType() {
			return this.ownerType;
		}

		public final StructRec<ObjectIRTypeOp> ancestorType() {
			return this.ancestorType;
		}

		public final StructRec<ObjectIRTypeOp> type() {
			return this.type;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.ownerType = data.addPtr("owner_type", OBJECT_TYPE);
			this.ancestorType = data.addPtr("ancestor_type", OBJECT_TYPE);
			this.type = data.addPtr("type", OBJECT_TYPE);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0120);
		}

	}

}
