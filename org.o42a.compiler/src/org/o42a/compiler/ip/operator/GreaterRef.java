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
import org.o42a.codegen.code.op.Int64op;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public class GreaterRef extends CompareConstructor {

	public GreaterRef(Phrase phrase) {
		super(phrase);
	}

	private GreaterRef(
			GreaterRef prototype,
			Reproducer reproducer,
			Ref phrase) {
		super(prototype, reproducer, phrase);
	}

	@Override
	protected boolean compare(long compareResult) {
		return compareResult > 0;
	}

	@Override
	protected GreaterRef reproduce(Reproducer reproducer, Ref phrase) {
		return new GreaterRef(this, reproducer, phrase);
	}

	@Override
	protected void write(CodeDirs dirs, Int64op comparisonValue) {

		final Code code = dirs.code();

		dirs.go(code, comparisonValue.gt(null, code, code.int64(0)));
	}

}
