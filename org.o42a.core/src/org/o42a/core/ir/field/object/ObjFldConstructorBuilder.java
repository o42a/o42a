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
import static org.o42a.core.ir.field.object.ObjectConstructorFn.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.FldCtrOp;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;


final class ObjFldConstructorBuilder
		extends AbstractObjFldConstructorBuilder {

	ObjFldConstructorBuilder(AbstractObjFld<?, ?> fld) {
		super(fld);
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

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
		final ObjFldOp<?, ?> fld =
				(ObjFldOp<?, ?>) host.field(dirs, fld().getKey());
		final FldCtrOp fctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		final Block constructed = code.addBlock("constructed");

		fctr.start(code, fld).goUnless(code, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final ObjectOp object = allocate(dirs, ctr, true);

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
					.newObject(dirs, tempObjHolder(dirs.getAllocator()))
					.toData(null, code);

			finish(code, fld, fctr, result);

			return;
		}

		ctr.fillObjectByAncestor(dirs, ds -> configure(ds, vmtc, object));

		final DataOp result =
				ctr.newObject(dirs, tempObjHolder(dirs.getAllocator()))
				.toData(null, code);

		finish(code, fld, fctr, result);
	}

	private void finish(
			Block code,
			ObjFldOp<?, ?> fld,
			FldCtrOp fctr,
			DataOp result) {
		fld.ptr().object(null, code).store(code, result, ACQUIRE_RELEASE);
		fctr.finish(code, fld);
		result.returnValue(code);
	}

}
