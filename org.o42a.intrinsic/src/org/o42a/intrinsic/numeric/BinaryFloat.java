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
import org.o42a.codegen.code.op.RecOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.BinaryResult;


abstract class BinaryFloat extends BinaryResult<Double, Double, Double> {

	BinaryFloat(
			Floats owner,
			String name,
			String leftOperandName,
			String rightOperandName) {
		super(
				owner,
				name,
				ValueType.FLOAT,
				leftOperandName,
				ValueType.FLOAT,
				rightOperandName,
				ValueType.FLOAT,
				"floats/" + name + ".o42a");
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
	protected void write(
			CodeDirs dirs,
			ValOp result,
			ValOp leftVal,
			ValOp rightVal) {

		final Code code = dirs.code();
		final AnyOp leftRec = leftVal.value(code.id("left_ptr"), code);
		final RecOp<Fp64op> leftPtr = leftRec.toFp64(null, code);
		final Fp64op left = leftPtr.load(code.id("left"), code);

		final AnyOp rightRec = rightVal.value(code.id("right_ptr"), code);
		final RecOp<Fp64op> rightPtr = rightRec.toFp64(null, code);
		final Fp64op right = rightPtr.load(code.id("right"), code);

		result.store(code, write(code, left, right));
	}

	protected abstract Fp64op write(Code code, Fp64op left, Fp64op right);

}
