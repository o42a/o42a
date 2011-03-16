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
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.object.*;
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
				host.ptr().writer().struct(code, getInstance()));
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
	protected void buildConstructor(
			ObjBuilder builder,
			Code code,
			CodePos exit) {

		final ObjOp host = builder.host();
		final ObjFld.Op fld =
			builder.getFunction().ptrArg(code, 1)
			.to(code, OBJ_FLD);
		final AnyOp previousPtr = fld.previous(code).load(code);

		final CondBlk construct =
			previousPtr.isNull(code).branch(code, "construct", "delegate");

		final AnyOp result1 = construct(
				builder,
				construct,
				exit,
				new ObjFldOp(this, host, fld)).toAny(construct);

		construct.go(code.tail());

		final CodeBlk delegate = construct.otherwise();
		final AnyOp result2 =
			delegate(builder, delegate, exit, previousPtr).toAny(delegate);

		delegate.go(code.tail());

		final AnyOp result = code.phi(result1, result2);

		final FldOp ownFld = host.field(code, exit, getField().getKey());
		final BoolOp isOwn = ownFld.ptr().eq(code, fld);
		final CondBlk store = isOwn.branch(code, "store", "do_not_store");

		fld.object(store).store(store, result);
		result.returnValue(store);

		result.returnValue(store.otherwise());
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			Code code,
			CodePos exit,
			AnyOp previousPtr) {
		code.dumpName("Delegate to ", previousPtr);

		final Op previous = previousPtr.to(code, getType().getType());
		final ObjectConstructorFunc constructor =
			previous.constructor(code).load(code);
		final AnyOp ancestorPtr = constructor.call(
				code,
				builder.host().toAny(code),
				previous.toAny(code));
		final ObjectOp ancestor =
			anonymousObject(builder, ancestorPtr, getBodyIR().getAscendant());

		return builder.newObject(
				code,
				exit,
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

		public final RecOp<AnyOp> previous(Code code) {
			return writer().ptr(code, getType().previous());
		}

		@Override
		protected AnyOp construct(
				Code code,
				ObjOp host,
				ObjectConstructorFunc constructor) {
			return constructor.call(code, host.toAny(code), toAny(code));
		}

	}

	public static final class Type
			extends RefFld.Type<Op, ObjectConstructorFunc> {

		private AnyPtrRec previous;

		private Type() {
		}

		public final AnyPtrRec previous() {
			return this.previous;
		}

		@Override
		public void allocate(SubData<Op> data) {
			super.allocate(data);
			this.previous = data.addPtr("previous");
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
		protected Signature<ObjectConstructorFunc> getSignature() {
			return OBJECT_CONSTRUCTOR;
		}

	}

}
