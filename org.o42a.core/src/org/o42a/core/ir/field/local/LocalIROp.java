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
package org.o42a.core.ir.field.local;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.field.local.LocalIR.Op;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.DefiniteIROp;


public final class LocalIROp extends DefiniteIROp {

	private final ObjectOp host;
	private final Op ptr;

	LocalIROp(ObjectOp host, Op ptr) {
		super(host.getBuilder());
		this.host = host;
		this.ptr = ptr;
	}

	@Override
	public final Op ptr() {
		return this.ptr;
	}

	@Override
	public final Op ptr(Code code) {
		return ptr();
	}

	public final ObjectOp host() {
		return this.host;
	}

}
