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
import static org.o42a.core.ir.field.object.ObjectConstructorFn.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.field.object.VmtChainAllocFn.VMT_CHAIN_ALLOC;
import static org.o42a.core.ir.object.op.ObjHolder.objTrap;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class ObjFld extends RefFld<StatefulOp, ObjectConstructorFn> {

	private FuncPtr<ObjectConstructorFn> cloneFunc;

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
	protected FuncPtr<ObjectConstructorFn> reuseConstructor() {

		final FuncPtr<ObjectConstructorFn> reused = super.reuseConstructor();

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
	protected ObjectConstructorFn.Signature getConstructorSignature() {
		return OBJECT_CONSTRUCTOR;
	}

	@Override
	protected FuncPtr<ObjectConstructorFn> constructorStub() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_constructor_stub", getConstructorSignature());
	}

	@Override
	protected FuncPtr<ObjectConstructorFn> cloneFunc() {
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
		final CtrOp.Op ctr =
				builder.getFunction()
				.arg(code, getConstructorSignature().ctr());
		final VmtIRChain.Op vmtc =
				builder.getFunction()
				.arg(code, getConstructorSignature().vmtc());
		final DataOp ancestorArg = ctr.ancestor(code).load(null, code);
		final ObjFldOp fld =
				(ObjFldOp) host.field(dirs, getField().getKey());

		// Ancestor is not specified for the first method in VMT chain.
		final BoolOp noAncestor = ancestorArg.isNull(null, code);

		// Initialize field construction in first method.
		final FldCtrOp fctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		final CondBlock start = noAncestor.branch(code, "start", "cont");

		final Block constructed = start.addBlock("constructed");

		fctr.start(start, fld).goUnless(start, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final ObjectOp evalAncestor = ancestor(dirs.sub(start));
		final DataOp evalAncestorPtr = initCtr(ctr, start, evalAncestor);

		start.go(code.tail());

		final Block cont = start.otherwise();

		final DataOp suppliedAncestorPtr = cont.phi(null, ancestorArg);

		cont.go(code.tail());

		final DataOp ancestorPtr = code.phi(
				ID.id("ancestor"),
				evalAncestorPtr,
				suppliedAncestorPtr);

		// Done initializing. Start the work.
		final VmtIRChain.Op prevVmtc = vmtc.prev(null, code).load(null, code);

		delegate(dirs, ctr, prevVmtc);
		updateVmtc(dirs, ctr);

		final Block dontConstruct = code.addBlock("dont_construct");

		noAncestor.goUnless(code, dontConstruct.head());
		if (dontConstruct.exists()) {
			// Not a first method in VMT chain. Just return NULL.
			dontConstruct.nullDataPtr().returnValue(dontConstruct);
		}

		// The first method constructs an object, stores it in the field,
		// finishes field construction, and returns new object.
		final DataOp result =
				construct(dirs, ctr, evalAncestor.phi(code, ancestorPtr))
				.toData(null, code);

		fld.ptr()
		.object(null, code)
		.store(code, result, ACQUIRE_RELEASE);

		fctr.finish(code, fld);

		result.returnValue(code);
	}

	@Override
	protected ObjFldOp op(Code code, ObjOp host, StatefulOp ptr) {
		return new ObjFldOp(this, host, ptr);
	}

	private DataOp initCtr(CtrOp.Op ctr, Code code, ObjectOp ancestor) {

		final DataOp ancestorPtr = ancestor.toData(null, code);

		ctr.ancestor(code).store(code, ancestorPtr);
		ctr.vmtc(code).store(
				code,
				ancestor.objectData(code)
				.ptr(code)
				.vmtc(code)
				.load(null, code));

		return ancestorPtr;
	}

	private ObjectOp ancestor(CodeDirs dirs) {
		return dirs.getBuilder()
				.objects()
				.objectAncestor(
						dirs,
						getField().toObject(),
						tempObjHolder(dirs.getAllocator()));
	}

	private void delegate(CodeDirs dirs, CtrOp.Op ctr, VmtIRChain.Op prevVmtc) {

		final Block code = dirs.code();
		final Block delegate = code.addBlock("delegate");

		prevVmtc.isNull(null, code).goUnless(code, delegate.head());

		final VmtIR vmtIR = getObjectIR().getVmtIR();
		final VmtIROp prevVmt = prevVmtc.loadVmt(delegate, vmtIR);

		prevVmt.compatible(delegate).goUnless(delegate, code.tail());

		final ObjectConstructorFn constructor =
				prevVmt.func(null, delegate, vmtConstructor())
				.load(null, delegate);

		// The field is dummy. The actual field is declared later.
		constructor.isNull(null, delegate).go(delegate, code.tail());

		delegate.dump("Delegate to ", prevVmtc);

		constructor.call(delegate, prevVmtc, ctr);

		delegate.go(code.tail());
	}

	private void updateVmtc(CodeDirs dirs, CtrOp.Op ctr) {

		final Block code = dirs.code();
		final FuncPtr<VmtChainAllocFn> allocFn =
				getGenerator().externalFunction().link(
						"o42a_obj_vmtc_alloc",
						VMT_CHAIN_ALLOC);
		final VmtIR vmtIR =
				getField().toObject().ir(getGenerator()).getVmtIR();
		final StructRecOp<VmtIRChain.Op> vmtcRec = ctr.vmtc(code);
		final VmtIRChain.Op newVmtc =
				allocFn.op(null, code)
				.allocate(dirs, vmtIR, vmtcRec.load(null, code));

		code.dump("Updated VMTC: ", newVmtc);

		vmtcRec.store(code, newVmtc);
	}

	private ObjectOp construct(CodeDirs dirs, CtrOp.Op ctr, ObjectOp ancestor) {

		final CodeBuilder builder = dirs.getBuilder();

		return ctr.op(builder)
		.host(builder.host())
		.fillAscendants(dirs, ancestor, getField().toObject())
		.newObject(dirs, objTrap());
	}

	private void buildCloneFunc(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final CtrOp.Op ctr =
				builder.getFunction()
				.arg(code, getConstructorSignature().ctr());
		final VmtIRChain.Op vmtc =
				builder.getFunction()
				.arg(code, getConstructorSignature().vmtc());
		final VmtIRChain.Op prevVmtc =
				vmtc.prev(null, code).load(null, code);
		final Block construct = code.addBlock("construct");

		prevVmtc.isNull(null, code).go(code, construct.head());

		constructor()
		.op(null, construct)
		.call(construct, vmtc, ctr)
		.returnValue(construct);

		final VmtIROp prevVmt =
				prevVmtc.loadVmt(code, getObjectIR().getVmtIR());

		prevVmt.compatible(code).goUnless(code, construct.head());

		code.dump("Delegate to ", prevVmtc);

		prevVmt.func(null, code, vmtConstructor())
		.load(null, code)
		.call(code, prevVmtc, ctr)
		.returnValue(code);
	}

}
