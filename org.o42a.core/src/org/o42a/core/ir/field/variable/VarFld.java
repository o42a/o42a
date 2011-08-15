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
package org.o42a.core.ir.field.variable;

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.ir.op.ObjectRefFunc.ObjectRef;
import org.o42a.core.member.field.Field;


public class VarFld extends RefFld<ObjectRefFunc> {

	public static final Type VAR_FLD = new Type();

	public VarFld(ObjectBodyIR bodyIR, Field<Link> field) {
		super(bodyIR, field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.VAR;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Field<Link> getField() {
		return (Field<Link>) super.getField();
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public VarFldOp op(Code code, ObjOp host) {
		return new VarFldOp(
				this,
				host,
				isOmitted() ? null : host.ptr().field(code, getInstance()));
	}

	@Override
	protected void fill() {
		super.fill();

		final Obj type =
				getField().getArtifact().getTypeRef().typeObject(dummyUser());
		final ObjectTypeIR typeIR = type.ir(getGenerator()).getStaticTypeIR();

		getInstance().targetType().setValue(
				typeIR.getInstance().pointer(getGenerator()));
	}

	@Override
	protected Type getType() {
		return VAR_FLD;
	}

	public static final class Op extends RefFld.Op<Op, ObjectRefFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRType.Op> targetType(Code code) {
			return ptr(null, code, getType().targetType());
		}

		@Override
		protected DataOp construct(
				Code code,
				ObjOp host,
				ObjectRefFunc constructor) {
			return constructor.call(code, host);
		}

	}

	public static final class Type extends RefFld.Type<Op, ObjectRefFunc> {

		private StructRec<ObjectIRType.Op> targetType;

		private Type() {
		}

		public final StructRec<ObjectIRType.Op> targetType() {
			return this.targetType;
		}

		@Override
		public void allocate(SubData<Op> data) {
			super.allocate(data);
			this.targetType = data.addPtr("target_type", OBJECT_TYPE);
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("VarFld");
		}

		@Override
		protected ObjectRef getSignature() {
			return OBJECT_REF;
		}

		@Override
		protected FuncPtr<ObjectRefFunc> constructorStub() {
			return getGenerator().externalFunction(
					"o42a_obj_ref_stub",
					OBJECT_REF);
		}

	}

}
