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

import static org.o42a.core.ir.op.Val.CONDITION_FLAG;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.UnaryResult;


public class IntegerMinus extends UnaryResult<Long, Long> {

	public IntegerMinus(Integers integers) {
		super(
				integers,
				"minus",
				ValueType.INTEGER,
				"operand",
				ValueType.INTEGER,
				"integers/minus.o42a");
	}

	@Override
	protected Long calculate(Long operand) {
		return -operand;
	}

	@Override
	protected void write(CodeDirs dirs, ValOp result, ValOp operand) {

		final Code code = dirs.code();
		final RecOp<Int32op> resultFlagsRec =
			result.flags(code.id("unary_flags_ptr"), code);

		resultFlagsRec.store(code, code.int32(CONDITION_FLAG));

		final RecOp<Int64op> operandPtr =
			operand.rawValue(code.id("operand_ptr"), code);
		final Int64op operandValue =
			operandPtr.load(code.id("operand_value"), code);
		final RecOp<Int64op> resultRec =
			result.rawValue(code.id("unary_value_tr"), code);

		resultRec.store(code, operandValue.neg(null, code));
	}

}
