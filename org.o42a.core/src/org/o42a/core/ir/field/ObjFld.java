/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.member.field.Field;


public class ObjFld extends RefFld {

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
		return getGenerator().objFldType();
	}

	@Override
	protected void fill() {
		super.fill();
		getInstance().getPrevious().setNull();
	}

	@Override
	protected void buildConstructor(
			ObjBuilder builder,
			Code code,
			CodePos exit) {

		final ObjOp host = builder.host();
		final ObjFldOp fld = op(code, host);
		final AnyOp previousPtr = fld.ptr().previous(code).load(code);

		final CondBlk construct =
			previousPtr.isNull(code).branch(code, "construct", "delegate");

		final AnyOp result1 =
			construct(builder, construct, exit, fld).toAny(construct);

		construct.go(code.tail());

		final CodeBlk delegate = construct.otherwise();
		final AnyOp result2 =
			delegate(builder, delegate, exit, previousPtr).toAny(delegate);

		delegate.go(code.tail());

		final AnyOp result = code.phi(result1, result2);

		final BoolOp homeHost =
			host.ptr().eq(code, getBodyIR().getPointer().op(code));
		final CondBlk store = homeHost.branch(code, "store", "do_not_store");

		fld.ptr().object(store).store(store, result);
		result.returnValue(store);

		result.returnValue(store.otherwise());
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			Code code,
			CodePos exit,
			AnyOp previousPtr) {

		final Op previous = previousPtr.to(code, getType().getOriginal());
		final ObjectRefFunc constructor =
			previous.constructor(code).load(code);
		final AnyOp ancestorPtr =
			constructor.call(code, builder.host().toAny(code));
		final ObjectOp ancestor =
			anonymousObject(builder, ancestorPtr, getBodyIR().getAscendant());

		return builder.newObject(
				code,
				exit,
				ancestor,
				getField().getArtifact().toObject(),
				CtrOp.FIELD_PROPAGATION);
	}

	public static final class Op extends RefFld.Op {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<AnyOp> previous(Code code) {
			return writer().ptr(code, getType().getPrevious());
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

	public static final class Type extends RefFld.Type<Op> {

		private AnyPtrRec previous;

		Type(IRGenerator generator) {
			super(generator, "ObjFld");
		}

		public final AnyPtrRec getPrevious() {
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

	}

}
