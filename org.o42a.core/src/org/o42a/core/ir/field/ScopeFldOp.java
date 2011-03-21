/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.member.MemberKey;


public class ScopeFldOp extends FldOp {

	ScopeFldOp(ScopeFld field, ObjOp host, ScopeFld.Op ptr) {
		super(field, host, ptr);
	}

	@Override
	public ScopeFld fld() {
		return (ScopeFld) super.fld();
	}

	@Override
	public ScopeFld.Op ptr() {
		return (ScopeFld.Op) super.ptr();
	}

	@Override
	public ObjectOp toObject(Code code, CodePos exit) {
		return target(code);
	}

	public ObjectOp target(Code code) {

		final ObjectBodyIR target = fld().getTarget();
		final DataOp targetPtr = ptr().object(code).load(code);

		return anonymousObject(
				getBuilder(),
				targetPtr,
				target.getAscendant());
	}

	@Override
	public FldOp field(Code code, CodePos exit, MemberKey memberKey) {
		return target(code).field(code, exit, memberKey);
	}

	@Override
	public ObjectOp materialize(Code code, CodePos exit) {
		return target(code);
	}

}
