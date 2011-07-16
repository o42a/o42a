/*
    Intrinsics
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.code.op.Fp64recOp;
import org.o42a.common.object.CompiledField;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.BinaryResult;


abstract class BinaryFloat extends BinaryResult<Double, Double, Double> {

	BinaryFloat(
			CompiledField field,
			String leftOperandName,
			String rightOperandName) {
		super(
				field,
				leftOperandName,
				ValueType.FLOAT,
				rightOperandName,
				ValueType.FLOAT);
	}

	@Override
	protected Double calculate(Resolver resolver, Double left, Double right) {
		try {
			return calculate(
					left.doubleValue(),
					((Number) right).doubleValue());
		} catch (ArithmeticException e) {
			if (reportError(resolver)) {
				resolver.getLogger().arithmeticError(
						resolver.getScope(),
						e.getMessage());
			}
			return null;
		}
	}

	protected abstract double calculate(double left, double right);

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final ValOp result = dirs.value();
		final AnyOp leftRec = leftVal.value(code.id("left_ptr"), code);
		final Fp64recOp leftPtr = leftRec.toFp64(null, code);
		final Fp64op left = leftPtr.load(code.id("left"), code);

		final AnyOp rightRec = rightVal.value(code.id("right_ptr"), code);
		final Fp64recOp rightPtr = rightRec.toFp64(null, code);
		final Fp64op right = rightPtr.load(code.id("right"), code);

		result.store(code, write(code, left, right));

		return result;
	}

	protected abstract Fp64op write(Code code, Fp64op left, Fp64op right);

}
