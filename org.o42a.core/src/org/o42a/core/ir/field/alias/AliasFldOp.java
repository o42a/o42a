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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.field.alias.AliasFld.Op;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.member.MemberKey;


final class AliasFldOp extends RefFldOp<Op, ObjectRefFunc> {

	private final Op ptr;

	AliasFldOp(RefFld<Op, ObjectRefFunc> fld, ObjOp host, Op ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final Op ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return objectFldValueOp();
	}

	@Override
	public FldOp<?> field(CodeDirs dirs, MemberKey memberKey) {
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
	protected DataOp construct(Code code, ObjectRefFunc constructor) {
		return constructor.call(code, host());
	}

}
