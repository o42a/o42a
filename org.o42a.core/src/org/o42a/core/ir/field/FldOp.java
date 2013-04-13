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
package org.o42a.core.ir.field;

import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.core.member.MemberKey;
import org.o42a.util.string.ID;


public abstract class FldOp<F extends Fld.Op<F>> extends FldIROp {

	public FldOp(ObjOp host, Fld<F> fld) {
		super(host, fld);
	}

	public final boolean isOmitted() {
		return fld().isOmitted();
	}

	@Override
	public ID getId() {
		if (!isOmitted()) {
			return super.getId();
		}
		return fld().getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fld<F> fld() {
		return (Fld<F>) super.fld();
	}

	@Override
	public abstract F ptr();

	@Override
	public abstract TargetOp field(CodeDirs dirs, MemberKey memberKey);

}
