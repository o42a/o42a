/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.common.ir;

import org.o42a.codegen.code.Code;
import org.o42a.common.def.Builtin;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;


public class BuiltinValueIR extends ProposedValueIR {

	private final Builtin builtin;

	public BuiltinValueIR(Builtin builtin, ObjectIR objectIR) {
		super(objectIR);
		this.builtin = builtin;
	}

	@Override
	protected void proposition(Code code, ValOp result, ObjectOp host) {
		this.builtin.writeBuiltin(code, result, host);
	}

}
