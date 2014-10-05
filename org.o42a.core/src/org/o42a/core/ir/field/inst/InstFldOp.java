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
package org.o42a.core.ir.field.inst;

import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldIROp;
import org.o42a.core.ir.object.ObjOp;


public abstract class InstFldOp<F extends StructOp<F>, T extends Type<F>>
		extends FldIROp<F, T> {

	public InstFldOp(ObjOp host, FldIR<F, T> fld, OpMeans<F> ptr) {
		super(host, fld, ptr);
	}

	@Override
	public InstFld<F, T> fld() {
		return (InstFld<F, T>) super.fld();
	}

}
