/*
    Compiler Core
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
package org.o42a.core.ir.local;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public final class RefLclOp extends LclOp {

	public static final Type REF_LCL = new Type();

	private final Op ptr;

	private RefLclOp(CodeBuilder builder, FieldIR fieldIR, Op ptr) {
		super(builder, fieldIR);
		this.ptr = ptr;
	}

	public final Obj getObject() {
		return getFieldIR().getField().toObject();
	}

	public Obj getAscendant() {
		return getObject();
	}

	@Override
	public final Op ptr() {
		return this.ptr;
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.dereference(dirs, holder);
	}

	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();
		final Obj ascendant = getAscendant();
		final DataOp objectPtr = ptr().object(code).load(null, code);

		objectPtr.isNull(null, code).go(code, dirs.falseDir());

		final ObjectOp target =
				anonymousObject(getBuilder(), objectPtr, ascendant);
		final ValueType<?> valueType =
				getFieldIR().getField().toObject().value().getValueType();

		if (valueType.isStateless()
				|| valueType.isLink() && valueType.isVariable()) {
			return holder.holdVolatile(code, target);
		}

		return holder.hold(code, target);
	}

	@Override
	public void write(Control control) {

		final Block code = control.code();
		final CodeDirs dirs =
				control.getBuilder().dirs(code, control.falseDir());
		final Obj object = getObject();

		// New objects created for the whole duration of the function.
		final ObjectOp newObject = getBuilder().newObject(
				dirs,
				tempObjHolder(
						control.getBuilder().getFunction().getAllocator()),
				null,
				getBuilder().objectAncestor(dirs, object),
				object);

		ptr().object(code).store(code, newObject.toData(null, code));
		newObject.value().writeCond(dirs);
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {

		final Code code = dirs.code();
		final ObjectOp object =
				value.materialize(dirs, tempObjHolder(code.getAllocator()));

		// Evaluate the condition prior to assigning to the target
		// to prevent infinite looping in situations like this:
		// I := I + 1
		object.value().writeCond(dirs);
		ptr().object(code).store(code, object.toData(null, code));
	}

	public static final class Op extends LclOp.Op<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		@Override
		public RefLclOp op(CodeBuilder builder, FieldIR fieldIR) {
			return new RefLclOp(builder, fieldIR, this);
		}

		public final DataRecOp object(Code code) {
			return ptr(null, code, getType().object());
		}

	}

	public static final class Type extends LclOp.Type<Op> {

		private DataRec object;

		private Type() {
			super(ID.id("RefLcl"));
		}

		public final DataRec object() {
			return this.object;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
		}

	}

}
