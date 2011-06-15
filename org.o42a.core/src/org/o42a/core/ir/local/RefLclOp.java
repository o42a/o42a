/*
    Compiler Core
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
package org.o42a.core.ir.local;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;


public final class RefLclOp extends LclOp {

	public static final Type REF_LCL = new Type();

	private RefLclOp(LocalBuilder builder, FieldIR<?> fieldIR, Op ptr) {
		super(builder, fieldIR, ptr);
	}

	public Obj getAscendant() {

		final Artifact<?> artifact = getFieldIR().getField().getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {
			return object;
		}

		return artifact.getTypeRef().typeObject(dummyUser());
	}

	@Override
	public Op ptr() {
		return (Op) super.ptr();
	}

	@Override
	public ObjOp toObject(CodeDirs dirs) {

		final Obj object = getFieldIR().getField().getArtifact().toObject();

		if (object == null) {
			return null;
		}

		return target(dirs);
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {

		final Obj object = getFieldIR().getField().getArtifact().toObject();

		if (object == null) {
			return null;
		}

		return target(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjOp materialize(CodeDirs dirs) {
		return target(dirs);
	}

	public ObjOp target(CodeDirs dirs) {

		final Code code = dirs.code();
		final Obj ascendant = getAscendant();
		final ObjectBodyIR ascendantBodyType =
			ascendant.ir(getGenerator()).getBodyType();
		final DataOp objectPtr = ptr().object(code).load(null, code);

		objectPtr.isNull(null, code).go(code, dirs.falseDir());

		return objectPtr.to(null, code, ascendantBodyType).op(
				getBuilder(),
				ascendant,
				DERIVED);
	}

	@Override
	public void write(Control control, ValOp result) {

		final Code code = control.code();
		final CodePos exit = control.exit();
		final CodeDirs dirs = control.getBuilder().falseWhenUnknown(code, exit);

		final Field<?> field = getFieldIR().getField();
		final Obj object = field.getArtifact().materialize();

		final ObjectOp newObject =
			getBuilder().newObject(dirs, object, CtrOp.NEW_INSTANCE);

		ptr().object(code).store(
				code,
				newObject.ptr().toAny(null, code).toData(null, code));
		newObject.writeLogicalValue(dirs);
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
		public RefLclOp op(LocalBuilder builder, FieldIR<?> fieldIR) {
			return new RefLclOp(builder, fieldIR, this);
		}

		public final DataRecOp object(Code code) {
			return ptr(null, code, getType().object());
		}

	}

	public static final class Type extends LclOp.Type<Op> {

		private DataRec object;

		private Type() {
		}

		public final DataRec object() {
			return this.object;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("RefLcl");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
		}

	}

}
