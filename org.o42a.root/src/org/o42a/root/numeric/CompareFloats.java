/*
    Root Object Definition
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.Op.EQ_ID;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.*;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.root.Root;


@SourcePath(relativeTo = Root.class, value = "floats/compare.o42a")
public final class CompareFloats extends CompareNumbers<Double> {

	public CompareFloats(Obj owner, AnnotatedSources sources) {
		super(owner, sources, ValueType.FLOAT);
	}

	@Override
	protected long compare(Double left, Double right) {
		return left.compareTo(right.doubleValue());
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Block code = dirs.code();
		final AnyOp leftRec = leftVal.value(LEFT_PTR_ID, code);
		final Fp64recOp leftPtr = leftRec.toFp64(null, code);
		final Fp64op left = leftPtr.load(LEFT_ID, code);

		final AnyOp rightRec = rightVal.value(RIGHT_PTR_ID, code);
		final Fp64recOp rightPtr = rightRec.toFp64(null, code);
		final Fp64op right = rightPtr.load(RIGHT_ID, code);

		final BoolOp gt = left.gt(GT_ID, code, right);
		final CondBlock greater = gt.branch(code, GREATER_ID, NOT_GREATER_ID);
		final Block notGreater = greater.otherwise();

		final ValType.Op result1 = intVal(dirs.getBuilder(), greater, 1);

		greater.go(code.tail());

		final BoolOp eq = left.eq(EQ_ID, notGreater, right);
		final ValType.Op result2 = eq.select(
				null,
				notGreater,
				intVal(dirs.getBuilder(), notGreater, 0),
				intVal(dirs.getBuilder(), notGreater, -1));

		notGreater.go(code.tail());

		final ValType.Op result = code.phi(null, result1, result2);

		return result.op(
				null,
				dirs.getBuilder(),
				type().getValueType(),
				NO_VAL_HOLDER);
	}

}
