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
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.ObjectsCode;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class ObjFld extends RefFld<StatefulOp, ObjectConstructorFunc> {

	private FuncPtr<ObjectConstructorFunc> cloneFunc;

	public ObjFld(Field field) {
		super(field, field.toObject());
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OBJ;
	}

	@Override
	public StatefulType getInstance() {
		return (StatefulType) super.getInstance();
	}

	@Override
	protected StatefulType getType() {
		return STATEFUL_FLD;
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
	protected ObjectConstructorFunc.Signature getConstructorSignature() {
		return OBJECT_CONSTRUCTOR;
	}

	@Override
	protected FuncPtr<ObjectConstructorFunc> constructorStub() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_constructor_stub", getConstructorSignature());
	}

	@Override
	protected FuncPtr<ObjectConstructorFunc> cloneFunc() {
		if (this.cloneFunc != null) {
			return this.cloneFunc;
		}
		return this.cloneFunc = getGenerator().newFunction().create(
				getField().getId().detail(CLONE_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildCloneFunc)).getPointer();
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final VmtIRChain.Op vmtc = builder.getFunction().arg(
				code,
				getConstructorSignature().vmtc());
		final ObjFldOp fld =
				(ObjFldOp) host.field(dirs, getField().getKey());
		final DataOp ancestorArg = builder.getFunction().arg(
				code,
				getConstructorSignature().ancestor());
		final BoolOp noAncestor = ancestorArg.isNull(null, code);
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

		final DataOp evaluatedAncestor = ancestor(builder, dirs, start);

		start.go(code.tail());

		final Block cont = start.otherwise();

		final DataOp suppliedAncestor = cont.phi(null, ancestorArg);
		cont.go(code.tail());

		final DataOp ancestor = code.phi(
				ID.id("ancestor"),
				evaluatedAncestor,
				suppliedAncestor);

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
						ancestor)
				.toData(null, construct);

		construct.go(code.tail());

		final Block delegate = construct.otherwise();
		final DataOp result2 = delegate(
				builder,
				builder.dirs(delegate, dirs.falseDir()),
				construct.head(),
				holder,
				prevVmtc,
				ancestor).toData(null, delegate);

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
	protected ObjFldOp op(Code code, ObjOp host, StatefulOp ptr) {
		return new ObjFldOp(this, host, ptr);
	}

	private DataOp ancestor(
			ObjBuilder builder,
			CodeDirs dirs,
			CondBlock code) {

		final CodeDirs ancDirs = dirs.sub(code);
		final DataOp ancestor =
				builder.objects()
				.objectAncestor(ancDirs, getField().toObject())
				.toData(null, code);

		code.dumpName("Ancestor: ", ancestor);
		ancDirs.done();

		return ancestor;
	}

	private ObjectOp construct(
			ObjBuilder builder,
			CodeDirs dirs,
			ObjHolder holder,
			DataOp ancestor) {

		final Obj object = getField().toObject();
		final ObjectsCode objects = builder.objects();

		return objects.newObject(
				dirs,
				builder.host(),
				holder,
				builder.host(),
				ancestor,
				object);
	}

	private ObjectOp delegate(
			ObjBuilder builder,
			CodeDirs dirs,
			CodePos construct,
			ObjFldTargetHolder holder,
			VmtIRChain.Op prevVmtc,
			DataOp ancestorPtr) {

		final Block code = dirs.code();

		code.dump("Delegate to ", prevVmtc);

		final VmtIR vmtIR = getObjectIR().getVmtIR();
		final VmtIROp prevVmt = prevVmtc.loadVmt(code, vmtIR);

		prevVmt.compatible(code).goUnless(code, construct);

		final ObjectConstructorFunc constructor =
				prevVmt.func(null, code, vmtConstructor()).load(null, code);

		// The field is dummy. The actual field is declared later.
		constructor.isNull(null, code).go(code, construct);

		final DataOp newAncestorPtr =
				constructor.call(code, builder.host(), prevVmtc, ancestorPtr);
		final ObjectOp ancestor = anonymousObject(
				dirs,
				newAncestorPtr,
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

	private void buildCloneFunc(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final VmtIRChain.Op vmtc = builder.getFunction().arg(
				code,
				getConstructorSignature().vmtc());
		final DataOp ancestorArg = builder.getFunction().arg(
				code,
				getConstructorSignature().ancestor());
		final VmtIRChain.Op prevVmtc = vmtc.prev(null, code).load(null, code);
		final CondBlock construct =
				prevVmtc.isNull(null, code)
				.branch(code, "construct", "delegate");

		constructor()
		.op(null, construct)
		.call(construct, host, vmtc, ancestorArg)
		.returnValue(construct);

		final Block delegate = construct.otherwise();
		final VmtIROp prevVmt =
				prevVmtc.loadVmt(delegate, getObjectIR().getVmtIR());

		prevVmt.compatible(delegate).goUnless(delegate, construct.head());
		prevVmt.func(null, delegate, vmtConstructor())
		.load(null, delegate)
		.call(delegate, host, prevVmtc, ancestorArg)
		.returnValue(delegate);
	}

}
