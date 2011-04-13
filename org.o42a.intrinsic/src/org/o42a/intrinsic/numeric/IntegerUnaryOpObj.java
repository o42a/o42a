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
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.common.adapter.UnaryOperatorInfo;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.UnaryOpObj;


public abstract class IntegerUnaryOpObj extends UnaryOpObj<Long, Long> {

	public IntegerUnaryOpObj(IntegerObject owner, UnaryOperatorInfo operator) {
		super(
				owner.getContainer(),
				operator,
				owner.getAncestor().toStatic(),
				ValueType.INTEGER,
				ValueType.INTEGER);
	}

	public static class Plus extends IntegerUnaryOpObj {

		public Plus(IntegerObject owner) {
			super(owner, UnaryOperatorInfo.PLUS);
		}

		@Override
		protected Long calculate(Long operand) {
			return operand;
		}

		@Override
		protected void write(
				CodeDirs dirs,
				ObjectOp host,
				RefOp operand,
				ValOp result) {
			operand.writeValue(dirs, result);
		}

	}

	public static final class Minus extends IntegerUnaryOpObj {

		public Minus(IntegerObject owner) {
			super(owner, UnaryOperatorInfo.MINUS);
		}

		@Override
		protected Long calculate(Long operand) {
			return -operand;
		}

		@Override
		protected void write(
				CodeDirs dirs,
				ObjectOp host,
				RefOp operand,
				ValOp result) {
			final Code code = dirs.code();

			operand.writeValue(dirs, result);

			final RecOp<Int64op> operandRec =
				result.rawValue(code.id("operand_ptr"), code);
			final Int64op operandVal =
				operandRec.load(code.id("operand_value"), code);

			operandRec.store(
					code,
					operandVal.neg(code.id("minus"), code));
		}

	}

}
