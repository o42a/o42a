/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.field.array;

import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.core.member.MemberKey;


public final class ArraySteOp extends FldOp<ArraySte.Op> {

	private final ArraySte.Op ptr;

	ArraySteOp(ObjOp host, ArraySte fld, ArraySte.Op ptr) {
		super(host, fld);
		this.ptr = ptr;
	}

	@Override
	public final ArraySte fld() {
		return (ArraySte) super.fld();
	}

	@Override
	public final ArraySte.Op ptr() {
		return this.ptr;
	}

	@Override
	public final HostValueOp value() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final TargetOp dereference(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final TargetOp field(CodeDirs dirs, MemberKey memberKey) {
		throw new UnsupportedOperationException();
	}

}
