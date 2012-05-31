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
package org.o42a.core.ir.field.object;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.field.object.ObjectConstructorFunc.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.object.ObjectConstructorFunc.ObjectConstructor;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;


public class ObjFld extends RefFld<ObjectConstructorFunc> {

	public static final Type OBJ_FLD = new Type();

	public ObjFld(ObjectBodyIR bodyIR, Field field) {
		super(bodyIR, field, field.toObject());
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OBJ;
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
				isOmitted() ? null : host.ptr().field(code, getInstance()));
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

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final ObjFld.Op fld =
				builder.getFunction().arg(code, OBJECT_CONSTRUCTOR.field());
		final ObjFldOp ownFld =
				(ObjFldOp) host.field(dirs, getField().getKey());
		final BoolOp isOwn =
				ownFld.toAny(null, code).eq(null, code, fld.toAny(null, code));
		final FldCtrOp ctr =
				code.getAllocator()
				.allocation()
				.allocate(code.id("fld_ctr"), FLD_CTR_TYPE);

		final Block start = code.addBlock("start");

		isOwn.go(code, start.head());
		if (start.exists()) {

			final Block constructed = start.addBlock("constructed");

			ctr.start(start, ownFld).goUnless(start, constructed.head());

			ownFld.ptr()
			.object(null, constructed)
			.load(null, constructed, ATOMIC)
			.toData(null, constructed)
			.returnValue(constructed);

			start.go(code.tail());
		}

		final DataOp previousPtr = fld.previous(null, code).load(null, code);

		final CondBlock construct =
				previousPtr.isNull(null, code)
				.branch(code, "construct", "delegate");
		final DataOp result1 = construct(
				builder,
				builder.dirs(construct, dirs.falseDir()))
				.toData(null, construct);

		construct.go(code.tail());

		final Block delegate = construct.otherwise();
		final DataOp result2 = delegate(
				builder,
				builder.dirs(delegate, dirs.falseDir()),
				previousPtr).toData(null, delegate);

		delegate.go(code.tail());

		final DataOp result = code.phi(null, result1, result2);

		final Block dontStore = code.addBlock("dont_store");

		isOwn.goUnless(code, dontStore.head());
		if (dontStore.exists()) {
			result.returnValue(dontStore);
		}

		fld.object(null, code).store(code, result, ACQUIRE_RELEASE);
		ctr.finish(code, ownFld);

		result.returnValue(code);
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			CodeDirs dirs,
			DataOp previousPtr) {

		final Code code = dirs.code();

		code.dumpName("Delegate to ", previousPtr);

		final Op previous = previousPtr.to(null, code, getType().getType());
		final ObjectConstructorFunc constructor =
				previous.constructor(null, code).load(null, code);
		final DataOp ancestorPtr =
				constructor.call(code, builder.host(), previous);
		final ObjectOp ancestor = anonymousObject(
				builder,
				ancestorPtr,
				getBodyIR().getAscendant());

		return builder.newObject(
				dirs,
				builder.host(),
				ancestor,
				getField().toObject());
	}

	public static final class Op extends RefFld.Op<Op, ObjectConstructorFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp previous(CodeId id, Code code) {
			return ptr(id, code, getType().previous());
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

		@Override
		public boolean isStateless() {
			return false;
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
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_fld_obj");
		}

		@Override
		protected ObjectConstructor getSignature() {
			return OBJECT_CONSTRUCTOR;
		}

		@Override
		protected FuncPtr<ObjectConstructorFunc> constructorStub() {
			return getGenerator()
					.externalFunction()
					.link("o42a_obj_constructor_stub", OBJECT_CONSTRUCTOR);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(
					"_O42A_DEBUG_TYPE_o42a_fld_obj",
					0x042a0200);
		}

	}

}
