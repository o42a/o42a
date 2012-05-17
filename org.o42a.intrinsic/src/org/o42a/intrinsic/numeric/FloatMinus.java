/*
    Intrinsics
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ir.value.Val.CONDITION_FLAG;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.code.op.Fp64recOp;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.value.ValueStruct;
import org.o42a.intrinsic.operator.UnaryResult;
import org.o42a.intrinsic.root.Root;


@SourcePath(relativeTo = Root.class, value = "floats/minus.o42a")
public final class FloatMinus extends UnaryResult<Double, Double> {

	public FloatMinus(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources, "operand", ValueStruct.FLOAT);
	}

	@Override
	protected Double calculate(Double operand) {
		return -operand;
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp operand) {

		final Code code = dirs.code();
		final ValOp result = dirs.value();
		final ValFlagsOp resultFlagsRec = result.flags("unary_flags", code);

		resultFlagsRec.store(code, CONDITION_FLAG);

		final Fp64recOp operandPtr =
				operand.value(code.id("operand_ptr"), code).toFp64(null, code);
		final Fp64op operandValue =
				operandPtr.load(code.id("operand_value"), code);
		final Fp64recOp resultRec =
				result.value(code.id("unary_value_tr"), code).toFp64(null, code);

		resultRec.store(code, operandValue.neg(null, code));

		return result;
	}

}
