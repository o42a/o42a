/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int8op;


public final class Int8CondOp implements CondOp {

	private final Int8op op;

	public Int8CondOp(Int8op op) {
		assert op != null :
			"Integer operation not specified";
		this.op = op;
	}

	@Override
	public final BoolOp loadCondition(CodeId id, Code code) {
		return this.op.lowestBit(null, code);
	}

	@Override
	public final BoolOp loadUnknown(CodeId id, Code code) {
		return this.op.lshr(null, code, 1).lowestBit(null, code);
	}

	@Override
	public void go(Code code, CodeDirs dirs) {
		dirs.go(code, this);
	}

	@Override
	public String toString() {
		return "CondOp[" + this.op + ']';
	}

}
