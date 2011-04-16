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

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;

import org.o42a.codegen.code.Code;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


final class NotEqualsRef extends ComparisonConstructor {

	NotEqualsRef(Phrase phrase) {
		super(phrase);
	}

	private NotEqualsRef(
			NotEqualsRef prototype,
			Reproducer reproducer,
			Ref phrase) {
		super(prototype, reproducer, phrase);
	}

	@Override
	protected boolean result(Value<?> value) {
		return value.isFalse();
	}

	@Override
	protected NotEqualsRef reproduce(Reproducer reproducer, Ref phrase) {
		return new NotEqualsRef(this, reproducer, phrase);
	}

	@Override
	protected ValOp writeComparison(CodeDirs dirs, ObjectOp comparison) {

		final Code code = dirs.code();
		final Code notEqual = code.addBlock("not_equal");
		final ValOp value =
			comparison.writeValue(falseWhenUnknown(code, notEqual.head()));

		dirs.goWhenFalse(code);
		if (notEqual.exists()) {
			notEqual.go(code.tail());
		}

		return value;
	}

	@Override
	protected void write(CodeDirs dirs, ValOp result, ValOp comparisonVal) {
		result.storeVoid(dirs.code());
	}

}
