/*
    Compiler Commons
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.common.ref.cmp;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


abstract class CompareOperator extends ComparisonOperator {

	private static final ID CMP_PTR_ID = ID.id("cmp_ptr");
	private static final ID CMP_VALUE_ID = ID.id("cmp_value");

	CompareOperator() {
		super(ClauseId.COMPARE);
	}

	@Override
	public final ValueType<?> getValueType() {
		return ValueType.INTEGER;
	}

	@Override
	public boolean checkForErrors(Ref phrase, CompilerLogger resolutionLogger) {

		final Resolution resolution = phrase.getResolution();

		if (resolution.isError()) {
			return true;
		}

		final ValueType<?> valueType = phrase.getValueType();

		if (valueType.is(ValueType.INTEGER)) {
			return false;
		}

		resolutionLogger.error(
				"comparison_not_integer",
				phrase,
				"Comparison expected to return integer value");

		return true;
	}

	@Override
	public final boolean result(Value<?> value) {
		if (value.getKnowledge().isFalse()) {
			return false;
		}

		final Long compareResult =
				ValueType.INTEGER.cast(value).getCompilerValue();

		return compare(compareResult);
	}

	@Override
	public ValOp write(ValDirs dirs, ValOp comparisonVal) {

		final Code code = dirs.code();
		final Int64recOp comparisonPtr =
				comparisonVal.rawValue(CMP_PTR_ID, code);
		final Int64op comparisonValue =
				comparisonPtr.load(CMP_VALUE_ID, code);

		write(dirs.dirs(), comparisonValue);

		return dirs.getBuilder().voidVal(code);
	}

	protected abstract boolean compare(long compareResult);

	protected abstract void write(CodeDirs dirs, Int64op comparisonValue);

}
