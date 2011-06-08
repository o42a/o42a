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
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.value.Value;


final class NotEqualsOperator extends ComparisonOperator {

	NotEqualsOperator() {
		super(ClauseId.EQUALS);
	}

	@Override
	public boolean result(Value<?> value) {
		return value.isFalse();
	}

	@Override
	public ValOp writeComparison(ValDirs dirs, ObjectOp comparison) {

		final Code code = dirs.code();
		final Code notEqual = code.addBlock("not_equal");

		comparison.writeLogicalValue(falseWhenUnknown(code, notEqual.head()));

		code.go(dirs.falseDir());
		if (notEqual.exists()) {
			notEqual.go(code.tail());
		}

		return voidValue().op(code);
	}

	@Override
	public ValOp write(ValDirs dirs, ValOp comparisonVal) {
		return voidValue().op(dirs.code());
	}

}
