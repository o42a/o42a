/*
    Intrinsics
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
package org.o42a.intrinsic.numeric;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;


public class IntegersEqual extends NumbersEqual<Long> {

	public IntegersEqual(Integers owner) {
		super(owner, "equals", ValueType.INTEGER, "integers/equals.o42a");
	}

	@Override
	protected boolean compare(Long left, Number right) {
		return left.longValue() == right.longValue();
	}

	@Override
	protected void write(
			CodeDirs dirs,
			ValOp result,
			ValOp leftVal,
			ValOp rightVal) {

		final Code code = dirs.code();
		final RecOp<Int64op> leftPtr =
			leftVal.rawValue(code.id("left_int_ptr"), code);
		final Int64op left = leftPtr.load(code.id("left"), code);

		final RecOp<Int64op> rightPtr =
			rightVal.rawValue(code.id("right_int_ptr"), code);
		final Int64op right = rightPtr.load(code.id("right"), code);

		final BoolOp equals = left.eq(code.id("eq"), code, right);

		dirs.go(code, equals);
		result.storeVoid(code);
	}

}
