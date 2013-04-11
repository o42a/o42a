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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.ir.op.TargetOp;


public class ObjFldOp extends RefFldOp<ObjFld.Op, ObjectConstructorFunc> {

	private final ObjFld.Op ptr;

	ObjFldOp(ObjFld fld, ObjOp host, ObjFld.Op ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final ObjFld fld() {
		return (ObjFld) super.fld();
	}

	@Override
	public final ObjFld.Op ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return objectFldValueOp();
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public TargetOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.dereference(dirs, holder);
	}

	@Override
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {
		return loadOrConstructTarget(dirs, holder, false);
	}

	@Override
	protected ObjectOp createObject(Block code, DataOp ptr) {
		if (!host().getPrecision().isExact()) {
			return super.createObject(code, ptr);
		}

		final ObjectIRBodyOp targetBodyPtr = ptr.to(
				null,
				code,
				fld().getTargetAscendant()
				.ir(getGenerator()).getBodyType());

		return targetBodyPtr.op(
				getBuilder(),
				fld().getTargetAscendant(),
				ObjectPrecision.COMPATIBLE);
	}

}
