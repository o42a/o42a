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
package org.o42a.core.ir.field.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.object.FldCtrOp.ALLOCATABLE_FLD_CTR;
import static org.o42a.core.ir.field.object.ObjectConstructorFunc.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.VmtIR.VMT_ID;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
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
				DERIVATION_USAGE)) {
			// Reuse the derived constructor if no run-time
			// inheritance expected.
			return constructorStub();
		}

		return null;
	}

	@Override
	protected FuncPtr<ObjectConstructorFunc> constructorStub() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_constructor_stub", OBJECT_CONSTRUCTOR);
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final VmtIRChain.Op vmtc =
				builder.getFunction().arg(code, OBJECT_CONSTRUCTOR.vmtc());
		final ObjFldOp fld =
				(ObjFldOp) host.field(dirs, getField().getKey());
		final ObjectIRDataOp ancestorDataArg = builder.getFunction().arg(
				code,
				OBJECT_CONSTRUCTOR.ancestorData());
		final BoolOp noAncestor = ancestorDataArg.isNull(null, code);
		final FldCtrOp ctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		final CondBlock start = noAncestor.branch(code, "start", "cont");

		final Block constructed = start.addBlock("constructed");

		ctr.start(start, fld).goUnless(start, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final ObjectIRDataOp evaluatedAncestorData =
				ancestorData(builder, dirs, start);

		start.go(code.tail());

		final Block cont = start.otherwise();

		final ObjectIRDataOp suppliedAncestorData =
				cont.phi(null, ancestorDataArg);
		cont.go(code.tail());

		final ObjectIRDataOp ancestorData = code.phi(
				ID.id("atype"),
				evaluatedAncestorData,
				suppliedAncestorData);

		final ObjFldTargetHolder holder =
				new ObjFldTargetHolder(code.getAllocator(), noAncestor);
		final VmtIRChain.Op prevVmtc =
				vmtc.prev(null, code).load(null, code);
		final CondBlock construct =
				prevVmtc.isNull(null, code)
				.branch(code, "construct", "delegate");
		final DataOp result1 =
				construct(
						builder,
						builder.dirs(construct, dirs.falseDir()),
						holder,
						ancestorData)
				.toData(null, construct);

		construct.go(code.tail());

		final Block delegate = construct.otherwise();
		final DataOp result2 = delegate(
				builder,
				builder.dirs(delegate, dirs.falseDir()),
				construct.head(),
				holder,
				prevVmtc,
				ancestorData).toData(null, delegate);

		delegate.go(code.tail());

		final DataOp result = code.phi(null, result1, result2);

		final Block dontStore = code.addBlock("dont_store");

		noAncestor.goUnless(code, dontStore.head());
		if (dontStore.exists()) {
			result.returnValue(dontStore);
		}

		fld.ptr().object(null, code).store(code, result, ACQUIRE_RELEASE);
		ctr.finish(code, fld);

		result.returnValue(code);
	}

	@Override
	protected ObjFldOp op(Code code, ObjOp host, Op ptr) {
		return new ObjFldOp(this, host, ptr);
	}

	private ObjectIRDataOp ancestorData(
			ObjBuilder builder,
			CodeDirs dirs,
			CondBlock code) {

		final CodeDirs ancDirs = dirs.sub(code);
		final ObjectIRDataOp ancestorData =
				builder.objects()
				.objectAncestor(ancDirs, getField().toObject())
				.objectData(code)
				.ptr();

		code.dumpName("Ancestor: ", ancestorData);
		ancDirs.done();

		return ancestorData;
	}

	private ObjectOp construct(
			ObjBuilder builder,
			CodeDirs dirs,
			ObjHolder holder,
			ObjectIRDataOp ancestorData) {

		final Block code = dirs.code();
		final Obj object = getField().toObject();
		final ObjectsCode objects = builder.objects();

		return objects.newObject(
				dirs,
				builder.host(),
				holder,
				builder.host(),
				ancestorData != null ? ancestorData :
				objects.objectAncestor(dirs, object)
				.objectData(code)
				.ptr(),
				object);
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			CodeDirs dirs,
			CodePos construct,
			ObjFldTargetHolder holder,
			VmtIRChain.Op prevVmtc,
			ObjectIRDataOp ancestorData) {

		final Block code = dirs.code();

		code.dump("Delegate to ", prevVmtc);

		final ObjectConstructorFunc constructor =
				prevVmtc.vmt(null, code)
				.load(null, code)
				.to(VMT_ID, code, getBodyIR().getVmtIR())
				.func(null, code, vmtConstructor())
				.load(null, code);

		// The field is dummy. The actual field is declared later.
		constructor.isNull(null, code).go(code, construct);

		final DataOp ancestorPtr =
				constructor.call(code, builder.host(), prevVmtc, ancestorData);
		final ObjectOp ancestor = anonymousObject(
				builder,
				ancestorPtr,
				getBodyIR().getClosestAscendant());

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

	}

	public static final class Type
			extends RefFld.Type<Op, ObjectConstructorFunc> {

		private Type() {
			super(ID.rawId("o42a_fld_obj"));
		}

		@Override
		public boolean isStateless() {
			return false;
		}

		@Override
		public boolean supportsVmt() {
			return true;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.OBJ.code());
		}

		@Override
		protected ObjectConstructorFunc.Signature getSignature() {
			return OBJECT_CONSTRUCTOR;
		}

	}

}
