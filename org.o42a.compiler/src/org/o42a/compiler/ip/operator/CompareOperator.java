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
import org.o42a.codegen.code.op.RecOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class CompareOperator extends ComparisonOperator {

	CompareOperator() {
		super(ClauseId.COMPARE);
	}

	@Override
	public boolean checkError(Ref phrase) {

		final Resolution resolution = phrase.getResolution();

		if (resolution.isError()) {
			return true;
		}

		final ValueType<?> valueType = phrase.getValueType();

		if (valueType == ValueType.INTEGER) {
			return false;
		}

		phrase.getLogger().error(
				"comparison_not_integer",
				phrase,
				"Comparison expected to return integer value");

		return true;
	}

	@Override
	public final boolean result(Value<?> value) {
		if (value.isFalse()) {
			return false;
		}

		final Long compareResult =
			ValueType.INTEGER.cast(value).getDefiniteValue();

		return compare(compareResult);
	}

	@Override
	public final void write(CodeDirs dirs, ValOp result, ValOp comparisonVal) {

		final Code code = dirs.code();
		final RecOp<Int64op> comparisonPtr =
			comparisonVal.rawValue(code.id("cmp_ptr"), code);
		final Int64op comparisonValue =
			comparisonPtr.load(code.id("cmp_value"), code);

		write(dirs, comparisonValue);
		result.storeVoid(code);
	}

	protected abstract boolean compare(long compareResult);

	protected abstract void write(CodeDirs dirs, Int64op comparisonValue);

}
