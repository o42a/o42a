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

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.object.FldCtrOp.ALLOCATABLE_FLD_CTR;
import static org.o42a.core.ir.field.object.ObjectConstructorFn.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.field.object.VmtChainAllocFn.VMT_CHAIN_ALLOC;
import static org.o42a.core.ir.object.ObjectOp.objectAncestor;
import static org.o42a.core.ir.object.op.ObjHolder.objTrap;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.field.RefVmtRecord;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


final class ObjVmtRecord
		extends RefVmtRecord<StatefulOp, StatefulType, ObjectConstructorFn>{

	private final Init<FuncPtr<ObjectConstructorFn>> cloneFunc =
			init(this::createCloneFunc);

	ObjVmtRecord(ObjFld fld) {
		super(fld);
	}

	@Override
	public final ObjectConstructorFn.Signature getConstructorSignature() {
		return OBJECT_CONSTRUCTOR;
	}

	@Override
	protected FuncPtr<ObjectConstructorFn> reuseConstructor() {

		final FuncPtr<ObjectConstructorFn> reused = super.reuseConstructor();

		if (reused != null) {
			return reused;
		}

		final FieldAnalysis analysis = fld().getField().getAnalysis();

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
	protected FuncPtr<ObjectConstructorFn> cloneFunc() {
		return this.cloneFunc.get();
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
				(ObjFldOp) host.field(dirs, fld().getKey());

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

		if (fld()
				.getField()
				.toObject()
				.value()
				.getStatefulness()
				.isExplicitEager()) {
			// Explicit eager object can be constructed without delegation.
			final Block eager = code.addBlock("eager");

			noAncestor.go(code, eager.head());

			final DataOp result =
					construct(
							dirs.sub(eager),
							ctr,
							evalAncestor.phi(eager, ancestorPtr))
					.toData(null, eager);

			finish(eager, fld, fctr, result);

			delegate(dirs, ctr, vmtc);
			updateVmtc(dirs, ctr);

			code.nullDataPtr().returnValue(code);

			return;
		}

		delegate(dirs, ctr, vmtc);
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

		finish(code, fld, fctr, result);
	}

	private FuncPtr<ObjectConstructorFn> createCloneFunc() {
		return getGenerator().newFunction().create(
				fld().getField().getId().detail(CLONE_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildCloneFunc)).getPointer();
	}

	private FuncPtr<ObjectConstructorFn> constructorStub() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_constructor_stub", getConstructorSignature());
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
		return objectAncestor(
				dirs,
				fld().getField().toObject(),
				tempObjHolder(dirs.getAllocator()));
	}

	private void delegate(CodeDirs dirs, CtrOp.Op ctr, VmtIRChain.Op vmtc) {

		final Block code = dirs.code();
		final VmtIRChain.Op prevVmtc = vmtc.prev(null, code).load(null, code);
		final Block delegate = code.addBlock("delegate");

		prevVmtc.isNull(null, code).goUnless(code, delegate.head());

		final VmtIR vmtIR = fld().getObjectIR().getVmtIR();
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
				fld().getField().toObject().ir(getGenerator()).getVmtIR();
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
		.fillAscendants(dirs, ancestor, fld().getField().toObject())
		.newObject(dirs, objTrap());
	}

	private void finish(
			Block code,
			ObjFldOp fld,
			FldCtrOp fctr,
			DataOp result) {
		fld.ptr(code).object(null, code).store(code, result, ACQUIRE_RELEASE);
		fctr.finish(code, fld);
		result.returnValue(code);
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
				prevVmtc.loadVmt(code, fld().getObjectIR().getVmtIR());

		prevVmt.compatible(code).goUnless(code, construct.head());

		code.dump("Delegate to ", prevVmtc);

		prevVmt.func(null, code, vmtConstructor())
		.load(null, code)
		.call(code, prevVmtc, ctr)
		.returnValue(code);
	}

}
