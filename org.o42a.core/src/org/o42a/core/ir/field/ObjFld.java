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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.field.ObjectConstructorFunc.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondCode;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.ObjectConstructorFunc.ObjectConstructor;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;


public class ObjFld extends RefFld<ObjectConstructorFunc> {

	public static final Type OBJ_FLD = new Type();

	public ObjFld(ObjectBodyIR bodyIR, Field<Obj> field) {
		super(bodyIR, field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OBJ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Field<Obj> getField() {
		return (Field<Obj>) super.getField();
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public ObjFldOp op(Code code, ObjOp host) {
		return new ObjFldOp(
				this,
				host,
				host.ptr().field(code, getInstance()));
	}

	@Override
	protected Type getType() {
		return OBJ_FLD;
	}

	@Override
	protected void fill() {
		super.fill();
		getInstance().previous().setNull();
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Code code = dirs.code();
		final ObjOp host = builder.host();
		final ObjFld.Op fld =
			builder.getFunction().arg(code, OBJECT_CONSTRUCTOR.field());
		final DataOp previousPtr = fld.previous(code).load(null, code);

		final CondCode construct =
			previousPtr.isNull(null, code)
			.branch(code, "construct", "delegate");
		final Code constructionFailed =
			construct.addBlock("construction_failed");

		final DataOp result1 = construct(
				builder,
				builder.falseWhenUnknown(construct, constructionFailed.head()),
				new ObjFldOp(this, host, fld)).toData(construct);

		if (constructionFailed.exists()) {
			constructionFailed.go(dirs.falseDir());
		}
		construct.go(code.tail());

		final Code delegate = construct.otherwise();
		final Code delegationFailed =
			delegate.addBlock("delegation_failed");
		final DataOp result2 = delegate(
				builder,
				builder.falseWhenUnknown(delegate, delegationFailed.head()),
				previousPtr).toData(delegate);

		if (delegationFailed.exists()) {
			delegationFailed.go(dirs.falseDir());
		}
		delegate.go(code.tail());

		final DataOp result = code.phi(null, result1, result2);

		final FldOp ownFld = host.field(dirs, getField().getKey());
		final BoolOp isOwn = ownFld.ptr().eq(null, code, fld);
		final CondCode store = isOwn.branch(code, "store", "do_not_store");

		fld.object(store).store(store, result);
		result.returnValue(store);

		result.returnValue(store.otherwise());
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			CodeDirs dirs,
			DataOp previousPtr) {
		final Code code = dirs.code();

		code.dumpName("Delegate to ", previousPtr);

		final Op previous = previousPtr.to(null, code, getType().getType());
		final ObjectConstructorFunc constructor =
			previous.constructor(code).load(null, code);
		final DataOp ancestorPtr =
			constructor.call(code, builder.host(), previous);
		final ObjectOp ancestor =
			anonymousObject(builder, ancestorPtr, getBodyIR().getAscendant());

		return builder.newObject(
				dirs,
				ancestor,
				getField().getArtifact().toObject(),
				CtrOp.PROPAGATION);
	}

	public static final class Op extends RefFld.Op<ObjectConstructorFunc> {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp previous(Code code) {
			return ptr(null, code, getType().previous());
		}

		@Override
		protected DataOp construct(
				Code code,
				ObjOp host,
				ObjectConstructorFunc constructor) {
			return constructor.call(code, host, this);
		}

	}

	public static final class Type
			extends RefFld.Type<Op, ObjectConstructorFunc> {

		private DataRec previous;

		private Type() {
		}

		public final DataRec previous() {
			return this.previous;
		}

		@Override
		public void allocate(SubData<Op> data) {
			super.allocate(data);
			this.previous = data.addDataPtr("previous");
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjFld");
		}

		@Override
		protected ObjectConstructor getSignature() {
			return OBJECT_CONSTRUCTOR;
		}

	}

}
