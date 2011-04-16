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

import org.o42a.ast.expression.BinaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.Distributor;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;


final class CompareEqualRef extends CompareConstructor {

	CompareEqualRef(BinaryNode node, Distributor distributor) {
		super(node, distributor);
	}

	private CompareEqualRef(
			CompareEqualRef prototype,
			Reproducer reproducer) {
		super(prototype, reproducer);
	}

	@Override
	public CompareEqualRef reproduce(Reproducer reproducer) {
		return new CompareEqualRef(this, reproducer);
	}

	@Override
	protected boolean compare(long compareResult) {
		return compareResult == 0;
	}

	@Override
	protected void write(CodeDirs dirs, Int64op comparisonValue) {

		final Code code = dirs.code();

		dirs.go(code, comparisonValue.eq(null, code, code.int64(0)));
	}

}
