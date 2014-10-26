/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.alias;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.FldCtrOp.ALLOCATABLE_FLD_CTR;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.alias.AliasField;


final class AliasVmtRecord
		extends ObjectRefVmtRecord<StatefulOp, StatefulType> {

	AliasVmtRecord(AliasFld fld) {
		super(fld);
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final FldOp<StatefulOp, StatefulType> fld =
				fld().op(code, builder.host());
		final FldCtrOp ctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		final Block constructed = code.addBlock("constructed");

		ctr.start(code, fld).goUnless(code, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final DataOp res = construct(builder, dirs).toData(null, code);

		fld.ptr().object(null, code).store(code, res, ACQUIRE_RELEASE);
		ctr.finish(code, fld);

		res.returnValue(code);
	}

	private ObjectOp construct(ObjBuilder builder, CodeDirs dirs) {

		final AliasField field = (AliasField) fld().getField();

		return field.getRef()
				.op(builder.host())
				.path()
				.materialize(dirs, tempObjHolder(dirs.getAllocator()));
	}

}
