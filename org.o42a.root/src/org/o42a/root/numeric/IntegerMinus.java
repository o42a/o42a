/*
    Root Object Definition
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
package org.o42a.root.numeric;

import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.root.Root;
import org.o42a.root.operator.UnaryResult;


@SourcePath(relativeTo = Root.class, value = "integers/minus.o42a")
public final class IntegerMinus extends UnaryResult<Long, Long> {

	public IntegerMinus(Obj owner, AnnotatedSources sources) {
		super(owner, sources, "operand", ValueType.INTEGER);
	}

	@Override
	protected Long calculate(Long operand) {
		return -operand;
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp operand) {

		final Code code = dirs.code();
		final ValOp result = dirs.value();
		final ValFlagsOp resultFlagsRec = result.flags(null, code);

		resultFlagsRec.store(code, VAL_CONDITION);

		final Int64recOp operandPtr = operand.rawValue(OPERAND_PTR_ID, code);
		final Int64op operandValue = operandPtr.load(OPERAND_VALUE_ID, code);
		final Int64recOp resultRec = result.rawValue(null, code);

		resultRec.store(code, operandValue.neg(null, code));

		return result;
	}

}
