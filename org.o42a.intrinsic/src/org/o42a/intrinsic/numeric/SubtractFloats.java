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
import org.o42a.codegen.code.op.Fp64op;


final class SubtractFloats extends BinaryFloat {

	SubtractFloats(Floats owner) {
		super(owner, "subtract", "left_operand", "right_operand");
	}

	@Override
	protected double calculate(double left, double right) {
		return left - right;
	}

	@Override
	protected Fp64op write(Code code, Fp64op left, Fp64op right) {
		return left.sub(code.id("sub"), code, right);
	}

}
