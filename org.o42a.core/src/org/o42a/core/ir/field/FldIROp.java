/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.DefiniteIROp;


public abstract class FldIROp<F extends StructOp<F>, T extends Type<F>>
		extends DefiniteIROp {

	private final ObjOp host;
	private final FldIR<F, T> fld;
	private final F ptr;

	public FldIROp(ObjOp host, FldIR<F, T> fld, F ptr) {
		super(host.getBuilder());
		this.host = host;
		this.fld = fld;
		this.ptr = ptr;
	}

	public final ObjOp host() {
		return this.host;
	}

	public FldIR<F ,T> fld() {
		return this.fld;
	}

	@Override
	public final F ptr() {
		return this.ptr;
	}

	@Override
	public final F ptr(Code code) {
		return ptr();
	}

}
