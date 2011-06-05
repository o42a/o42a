/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.op.CodeDirs;


final class GreaterOperator extends CompareOperator {

	@Override
	public boolean compare(long compareResult) {
		return compareResult > 0;
	}


	@Override
	public void write(CodeDirs dirs, Int64op comparisonValue) {

		final Code code = dirs.code();
		final BoolOp result = comparisonValue.gt(null, code, code.int64(0));

		result.goUnless(code, dirs.falseDir());
	}

}
