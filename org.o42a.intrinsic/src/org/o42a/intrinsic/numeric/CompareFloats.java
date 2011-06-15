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
import org.o42a.codegen.code.CondCode;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueType;


final class CompareFloats extends CompareNumbers<Double> {

	CompareFloats(Floats owner) {
		super(
				owner.toMemberOwner(),
				"compare",
				ValueType.FLOAT,
				"floats/compare.o42a");
	}

	@Override
	protected long compare(Double left, Double right) {
		return left.compareTo(right.doubleValue());
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final AnyOp leftRec = leftVal.value(code.id("left_ptr"), code);
		final Fp64recOp leftPtr =
			leftRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op left = leftPtr.load(code.id("left"), code);

		final AnyOp rightRec = rightVal.value(code.id("right_ptr"), code);
		final Fp64recOp rightPtr =
			rightRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op right = rightPtr.load(code.id("right"), code);

		final BoolOp gt = left.gt(code.id("gt"), code, right);
		final CondCode greater = gt.branch(code, "greater", "not_greater");
		final Code notGreater = greater.otherwise();

		final ValType.Op result1 = ONE.op(dirs.getBuilder(), greater).ptr();

		greater.go(code.tail());

		final BoolOp eq = left.eq(notGreater.id("eq"), notGreater, right);
		final ValType.Op result2 = eq.select(
				null,
				notGreater,
				ZERO.op(dirs.getBuilder(), notGreater).ptr(),
				MINUS_ONE.op(dirs.getBuilder(), notGreater).ptr());

		notGreater.go(code.tail());

		final ValType.Op result = code.phi(null, result1, result2);

		return result.op(dirs.getBuilder(), getValueType());
	}

}
