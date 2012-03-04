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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;


public class ScopeFldOp extends FldOp {

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

	public ObjectOp target(CodeDirs dirs) {
		if (isOmitted()) {

			final Obj target = fld().getField().getArtifact().toObject();
			final ObjectIR targetIR = target.ir(getGenerator());

			return targetIR.op(getBuilder(), dirs.code());
		}

		final Code code = dirs.code();
		final ObjectBodyIR target = fld().getTarget();
		final DataOp targetPtr = ptr().object(code).load(null, code);

		return anonymousObject(
				getBuilder(),
				targetPtr,
				target.getAscendant());
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs) {
		return target(dirs);
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

}
