/*
    Root Object Definition
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.root.numeric;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueType;
import org.o42a.root.operator.BinaryResult;


abstract class BinaryInteger extends BinaryResult<Long, Long, Long> {

	public BinaryInteger(
			Obj owner,
			AnnotatedSources sources,
			String leftOperandName,
			String rightOperandName) {
		super(
				owner,
				sources,
				leftOperandName,
				ValueType.INTEGER,
				rightOperandName,
				ValueType.INTEGER);
	}

	@Override
	protected Long calculate(Resolver resolver, Long left, Long right) {
		try {
			return calculate(left.longValue(), ((Number) right).longValue());
		} catch (ArithmeticException e) {
			if (reportError(resolver)) {
				resolver.getLogger().arithmeticError(
						resolver.getLocation(),
						e.getMessage());
			}
			return null;
		}
	}

	protected abstract long calculate(long left, long right);

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final ValOp result = dirs.value();
		final Int64recOp leftPtr =
				leftVal.rawValue(LEFT_PTR_ID, code);
		final Int64op left = leftPtr.load(LEFT_ID, code);

		final Int64recOp rightPtr =
				rightVal.rawValue(RIGHT_PTR_ID, code);
		final Int64op right = rightPtr.load(RIGHT_ID, code);

		result.store(code, write(code, left, right));

		return result;
	}

	protected abstract Int64op write(Code code, Int64op left, Int64op right);

}
