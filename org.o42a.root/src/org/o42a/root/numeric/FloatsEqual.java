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

import static org.o42a.codegen.code.op.Op.EQ_ID;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.*;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.root.Root;


@SourcePath(relativeTo = Root.class, value = "floats/equals.o42a")
public final class FloatsEqual extends NumbersEqual<Double> {

	public FloatsEqual(Obj owner, AnnotatedSources sources) {
		super(owner, sources, ValueType.FLOAT);
	}

	@Override
	protected boolean compare(Double left, Double right) {
		return left.doubleValue() == right.doubleValue();
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

		final BoolOp equals = left.eq(EQ_ID, code, right);

		equals.goUnless(code, dirs.falseDir());

		return dirs.getBuilder().voidVal(code);
	}

}
