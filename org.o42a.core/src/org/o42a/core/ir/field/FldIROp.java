/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.IROp;


public abstract class FldIROp extends IROp implements HostOp {

	private final ObjOp host;
	private final FldIR fld;

	public FldIROp(ObjOp host, FldIR fld) {
		super(host.getBuilder());
		this.host = host;
		this.fld = fld;
	}

	public final ObjOp host() {
		return this.host;
	}

	public FldIR fld() {
		return this.fld;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

}