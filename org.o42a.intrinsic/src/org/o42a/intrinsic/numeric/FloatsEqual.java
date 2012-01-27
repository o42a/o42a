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
import org.o42a.codegen.code.op.*;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.value.ValueStruct;
import org.o42a.intrinsic.root.Root;


@SourcePath(relativeTo = Root.class, value = "floats/equals.o42a")
public final class FloatsEqual extends NumbersEqual<Double> {

	public FloatsEqual(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources, ValueStruct.FLOAT);
	}

	@Override
	protected boolean compare(Double left, Double right) {
		return left.doubleValue() == right.doubleValue();
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final AnyOp leftRec = leftVal.value(code.id("left_ptr"), code);
		final Fp64recOp leftPtr =
				leftRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op left = leftPtr.load(code.id("left"), code);

		final AnyOp rightRec = rightVal.value(code.id("right_ptr"), code);
		final Fp64recOp rightPtr =
				rightRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op right = rightPtr.load(code.id("right"), code);

		final BoolOp equals = left.eq(code.id("eq"), code, right);

		equals.goUnless(code, dirs.falseDir());

		return voidValue().op(dirs.getBuilder(), code);
	}

}
