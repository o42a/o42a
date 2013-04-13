/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.field.object.ObjectConstructorFunc.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.ObjectsCode;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class ObjFld extends RefFld<ObjFld.Op, ObjectConstructorFunc> {

	public static final Type OBJ_FLD = new Type();

	public ObjFld(Field field) {
		super(field, field.toObject());
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
	protected Obj targetType(Obj bodyType) {
		return bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());
	}

	@Override
	protected FuncPtr<ObjectConstructorFunc> reuseConstructor() {

		final FuncPtr<ObjectConstructorFunc> reused = super.reuseConstructor();

		if (reused != null) {
			return reused;
		}

		final FieldAnalysis analysis = getField().getAnalysis();

		if (!analysis.derivation().isUsed(
				getGenerator().getAnalyzer(),
				RUNTIME_DERIVATION_USAGE)) {
			// Reuse the derived constructor if no run-time
			// inheritance expected.
			return getType().constructorStub();
		}

		return null;
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
				.allocate(FLD_CTR_ID, FLD_CTR_TYPE);

		final CondBlock start = isOwn.branch(code, "start", "cont");

		final Block constructed = start.addBlock("constructed");

		ctr.start(start, ownFld).goUnless(start, constructed.head());

		ownFld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final ObjectIRTypeOp evaluatedAncestorType =
				ancestorType(builder, dirs, start);

		start.go(code.tail());

		final Block cont = start.otherwise();
		final ObjectIRTypeOp suppliedAncestorType = builder.getFunction().arg(
				cont,
				OBJECT_CONSTRUCTOR.ancestorType());

		cont.go(code.tail());

		final ObjectIRTypeOp ancestorType = code.phi(
				ID.id("atype"),
				evaluatedAncestorType,
				suppliedAncestorType);

		final ObjFldTargetHolder holder =
				new ObjFldTargetHolder(code.getAllocator(), isOwn);
		final Op previous = fld.previous(null, code).load(null, code);
		final CondBlock construct =
				previous.isNull(null, code)
				.branch(code, "construct", "delegate");
		final DataOp result1 =
				construct(
						builder,
						builder.dirs(construct, dirs.falseDir()),
						holder,
						ancestorType)
				.toData(null, construct);

		construct.go(code.tail());

		final Block delegate = construct.otherwise();
		final DataOp result2 = delegate(
				builder,
				builder.dirs(delegate, dirs.falseDir()),
				holder,
				previous,
				ancestorType).toData(null, delegate);

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

	private ObjectIRTypeOp ancestorType(
			ObjBuilder builder,
			CodeDirs dirs,
			CondBlock code) {

		final CodeDirs ancDirs = dirs.sub(code);
		final ObjectIRTypeOp ancestorType =
				builder.objects()
				.objectAncestor(ancDirs, getField().toObject())
				.objectType(code)
				.ptr();

		code.dumpName("Ancestor: ", ancestorType);
		ancDirs.done();

		return ancestorType;
	}

	private ObjectOp construct(
			ObjBuilder builder,
			CodeDirs dirs,
			ObjHolder holder,
			ObjectIRTypeOp ancestorType) {

		final Obj object = getField().toObject();
		final ObjectsCode objects = builder.objects();

		return objects.newObject(
				dirs,
				builder.host(),
				holder,
				builder.host(),
				ancestorType != null ? ancestorType :
				objects.objectAncestor(dirs, object)
				.objectType(dirs.code())
				.ptr(),
				object);
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			CodeDirs dirs,
			ObjFldTargetHolder holder,
			Op previous,
			ObjectIRTypeOp ancestorType) {

		final Block code = dirs.code();

		code.dumpName("Delegate to ", previous);

		final ObjectConstructorFunc constructor =
				previous.constructor(null, code).load(null, code);
		final DataOp ancestorPtr =
				constructor.call(code, builder.host(), previous, ancestorType);
		final ObjectOp ancestor = anonymousObject(
				builder,
				ancestorPtr,
				getBodyIR().getAscendant());

		// Ancestor object is marked used by previous constructor.
		// Set it to a temporary holder, to automatically release.
		tempObjHolder(dirs.getAllocator()).set(code, ancestor);

		return builder.objects().newObject(
				dirs,
				builder.host(),
				holder,
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

		public final StructRecOp<Op> previous(ID id, Code code) {
			return ptr(id, code, getType().previous());
		}

		@Override
		protected DataOp construct(
				Code code,
				ObjOp host,
				ObjectConstructorFunc constructor) {
			return constructor.call(code, host, this, null);
		}

	}

	public static final class Type
			extends RefFld.Type<Op, ObjectConstructorFunc> {

		private StructRec<Op> previous;

		private Type() {
			super(ID.rawId("o42a_fld_obj"));
		}

		@Override
		public boolean isStateless() {
			return false;
		}

		public final StructRec<Op> previous() {
			return this.previous;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			super.allocate(data);
			this.previous = data.addPtr("previous", OBJ_FLD);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.OBJ.code());
		}

		@Override
		protected ObjectConstructorFunc.Signature getSignature() {
			return OBJECT_CONSTRUCTOR;
		}

		@Override
		protected FuncPtr<ObjectConstructorFunc> constructorStub() {
			return getGenerator()
					.externalFunction()
					.link("o42a_obj_constructor_stub", OBJECT_CONSTRUCTOR);
		}

	}

}
