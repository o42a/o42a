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

import static org.o42a.core.ir.object.op.CtrOp.allocateCtr;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.StructRec;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.member.MemberKey;


public class ObjFldOp extends RefFldOp<
		StatefulOp,
		StatefulType,
		Ptr<ObjFldConf.Op>,
		StructRec<ObjFldConf.Op>,
		ObjectConstructorFn> {

	ObjFldOp(ObjOp host, ObjFld fld, OpMeans<RefFld.StatefulOp> ptr) {
		super(host, fld, ptr);
	}

	@Override
	public final ObjFld fld() {
		return (ObjFld) super.fld();
	}

	@Override
	public HostValueOp value() {
		return objectFldValueOp();
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.dereference(dirs, holder);
	}

	@Override
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {
		return loadOrConstructTarget(dirs, holder, false);
	}

	@Override
	protected DataOp construct(
			Code code,
			ObjectConstructorFn constructor,
			VmtIRChain.Op vmtc) {

		final CtrOp ctr = allocateCtr(getBuilder(), code);
		final CtrOp.Op ptr = ctr.ptr(code);

		ctr.fillOwner(code, host());
		ptr.object(code).store(code, code.nullDataPtr());

		return constructor.call(code, vmtc, ctr);
	}

	@Override
	protected ObjectConstructorFn constructor(Code code, VmtIRChain.Op vmtc) {
		return fld().conf().constructor().op(null, code);
	}

	@Override
	protected ObjectOp createObject(Block code, DataOp ptr) {
		if (!host().getPrecision().isExact()) {
			return super.createObject(code, ptr);
		}

		final ObjectIR ir = fld().getTargetAscendant().ir(getGenerator());

		return ir.compatibleOp(
				getBuilder(),
				code.means(c -> ptr.to(null, c, ir.getType())));
	}

}
