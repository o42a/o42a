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

import org.o42a.codegen.CodeId;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.member.MemberKey;


public abstract class FldOp extends IROp implements HostOp {

	private final Fld fld;
	private final ObjOp host;

	public FldOp(Fld fld, ObjOp host, Fld.Op<?> ptr) {
		super(host.getBuilder(), ptr);
		this.fld = fld;
		this.host = host;
	}

	public final boolean isOmitted() {
		return this.fld.isOmitted();
	}

	@Override
	public CodeId getId() {
		if (!isOmitted()) {
			return super.getId();
		}
		return fld().getField().ir(getGenerator()).getId();
	}

	public Fld fld() {
		return this.fld;
	}

	public final ObjOp host() {
		return this.host;
	}

	@Override
	public Fld.Op<?> ptr() {
		return (Fld.Op<?>) super.ptr();
	}

	@Override
	public ObjectOp toObject(CodeDirs dirs) {
		return null;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	@Override
	public abstract FldOp field(CodeDirs dirs, MemberKey memberKey);

}
