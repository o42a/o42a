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
import static org.o42a.core.ir.field.FldCtrOp.ALLOCATABLE_FLD_CTR;
import static org.o42a.core.ir.field.RefVmtRecord.FLD_CTR_ID;
import static org.o42a.core.ir.field.object.ObjFldConfigureFn.OBJ_FLD_CONFIGURE;
import static org.o42a.core.ir.field.object.ObjectConstructorFn.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.ObjectOp.objectAncestor;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.objTrap;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.FldCtrOp;
import org.o42a.core.ir.field.RefVmtRecord;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;


final class ObjFldConstructorBuilder
		implements FunctionBuilder<ObjectConstructorFn> {

	private final ObjFld fld;

	ObjFldConstructorBuilder(ObjFld fld) {
		this.fld = fld;
	}

	public final Generator getGenerator() {
		return fld().getGenerator();
	}

	public final ObjFld fld() {
		return this.fld;
	}

	@Override
	public void build(Function<ObjectConstructorFn> constructor) {

		final Block failure = constructor.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				constructor,
				failure.head(),
				fld().getObjectIR(),
				COMPATIBLE_OBJECT);
		final CodeDirs dirs = builder.dirs(constructor, failure.head());
		final CodeDirs subDirs =
				dirs.begin(
						RefVmtRecord.CONSTRUCT_ID,
						"Constructing field `" + fld().getField() + "`");

		buildConstructor(builder, subDirs);

		subDirs.done();

		if (failure.exists()) {
			failure.nullDataPtr().returnValue(failure);
		}
	}

	private void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final CtrOp ctr =
				builder.getFunction()
				.arg(code, OBJECT_CONSTRUCTOR.ctr())
				.op(builder)
				.host(host)
				.sample(fld().getTarget());
		final VmtIRChain.Op vmtc =
				builder.getFunction().arg(code, OBJECT_CONSTRUCTOR.vmtc());
		final ObjFldOp fld =
				(ObjFldOp) host.field(dirs, fld().getKey());

		// Initialize field construction in first method.
		final FldCtrOp fctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		final Block constructed = code.addBlock("constructed");

		fctr.start(code, fld).goUnless(code, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final ObjectOp object = allocate(dirs, ctr);

		if (fld()
				.getField()
				.toObject()
				.value()
				.getStatefulness()
				.isExplicitEager()) {
			// Explicit eager object can be constructed without
			// any configuration.
			final DataOp result =
					ctr.fillObject(dirs)
					.newObject(dirs, objTrap())
					.toData(null, code);

			finish(code, fld, fctr, result);

			return;
		}

		object.objectData(code)
		.ptr()
		.vmtc(code)
		.store(code, ctr.ptr(code).vmtc(code).load(null, code));

		ctr.fillObjectByAncestor(dirs, ds -> configure(ds, vmtc, object));

		ctr.ptr(code)
		.vmtc(code)
		.store(code, object.objectData(code).ptr().vmtc(code).load(null, code));

		final DataOp result =
				ctr.newObject(dirs, objTrap()).toData(null, code);

		finish(code, fld, fctr, result);
	}

	private void configure(CodeDirs dirs, VmtIRChain.Op vmtc, ObjectOp object) {

		final Block code = dirs.code();

		getGenerator()
		.externalFunction()
		.link("o42a_fld_obj_configure", OBJ_FLD_CONFIGURE)
		.op(null, code)
		.configure(
				dirs,
				vmtc,
				object.ptr(),
				fld().vmtRecord().recordOffset().op(null, code));
	}

	private ObjectOp allocate(CodeDirs dirs, CtrOp ctr) {
		ctr.ancestor(ancestor(dirs));

		final Block code = dirs.code();

		ctr.allocateObject(dirs);
		ctr.fillAncestor(code);

		final VmtIRChain.Op inheritedVmtc = ctr.getAncestor().vmtc(code);

		ctr.ptr(code).vmtc(code).store(code, inheritedVmtc);

		return ctr.object(code);
	}

	private ObjectOp ancestor(CodeDirs dirs) {
		return objectAncestor(
				dirs,
				fld().getTarget(),
				tempObjHolder(dirs.getAllocator()));
	}

	private void finish(
			Block code,
			ObjFldOp fld,
			FldCtrOp fctr,
			DataOp result) {
		fld.ptr().object(null, code).store(code, result, ACQUIRE_RELEASE);
		fctr.finish(code, fld);
		result.returnValue(code);
	}

}
