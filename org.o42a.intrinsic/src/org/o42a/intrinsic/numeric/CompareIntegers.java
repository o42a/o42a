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

import static org.o42a.codegen.code.op.Op.EQ_ID;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.value.ValueStruct;
import org.o42a.intrinsic.root.Root;


@SourcePath(relativeTo = Root.class, value = "integers/compare.o42a")
public final class CompareIntegers extends CompareNumbers<Long> {

	public CompareIntegers(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources, ValueStruct.INTEGER);
	}

	@Override
	protected long compare(Long left, Long right) {
		return left.compareTo(right.longValue());
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Block code = dirs.code();
		final Int64recOp leftPtr = leftVal.rawValue(LEFT_PTR_ID, code);
		final Int64op left = leftPtr.load(LEFT_ID, code);

		final Int64recOp rightPtr = rightVal.rawValue(RIGHT_PTR_ID, code);
		final Int64op right = rightPtr.load(RIGHT_ID, code);

		final BoolOp gt = left.gt(GT_ID, code, right);
		final CondBlock greater = gt.branch(code, GREATER_ID, NOT_GREATER_ID);
		final Block notGreater = greater.otherwise();

		final ValType.Op result1 = ONE.op(dirs.getBuilder(), greater).ptr();

		greater.go(code.tail());

		final BoolOp eq = left.eq(EQ_ID, notGreater, right);
		final ValType.Op result2 = eq.select(
				null,
				notGreater,
				ZERO.op(dirs.getBuilder(), notGreater).ptr(),
				MINUS_ONE.op(dirs.getBuilder(), notGreater).ptr());

		notGreater.go(code.tail());

		final ValType.Op result = code.phi(null, result1, result2);

		return result.op(
				null,
				dirs.getBuilder(),
				value().getValueType(),
				NO_VAL_HOLDER);
	}

}
