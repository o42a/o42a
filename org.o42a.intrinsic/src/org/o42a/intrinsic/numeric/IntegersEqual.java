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

import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.value.ValueStruct;
import org.o42a.intrinsic.root.Root;


@SourcePath(relativeTo = Root.class, value = "integers/equals.o42a")
public final class IntegersEqual extends NumbersEqual<Long> {

	public IntegersEqual(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources, ValueStruct.INTEGER);
	}

	@Override
	protected boolean compare(Long left, Long right) {
		return left.longValue() == right.longValue();
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final Int64recOp leftPtr =
				leftVal.rawValue(code.id("left_int_ptr"), code);
		final Int64op left = leftPtr.load(code.id("left"), code);

		final Int64recOp rightPtr =
				rightVal.rawValue(code.id("right_int_ptr"), code);
		final Int64op right = rightPtr.load(code.id("right"), code);

		final BoolOp equals = left.eq(code.id("eq"), code, right);

		equals.goUnless(code, dirs.falseDir());

		return voidValue().op(dirs.getBuilder(), code);
	}

}
