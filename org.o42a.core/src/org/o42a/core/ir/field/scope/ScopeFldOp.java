/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ir.field.scope;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldFldOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;


public class ScopeFldOp extends FieldFldOp {

	private final ScopeFld.Op ptr;

	ScopeFldOp(ScopeFld field, ObjOp host, ScopeFld.Op ptr) {
		super(field, host);
		this.ptr = ptr;
	}

	@Override
	public final ScopeFld fld() {
		return (ScopeFld) super.fld();
	}

	@Override
	public final ScopeFld.Op ptr() {
		return this.ptr;
	}

	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {
		if (isOmitted()) {

			final Obj target = fld().getField().toObject();
			final ObjectIR targetIR = target.ir(getGenerator());

			return targetIR.op(getBuilder(), dirs.code());
		}

		final Block code = dirs.code();
		final ObjectBodyIR target = fld().getTarget();
		final DataOp targetPtr = ptr().object(code).load(null, code);

		return holder.hold(
				code,
				anonymousObject(
						getBuilder(),
						targetPtr,
						target.getAscendant()));
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
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
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

}
