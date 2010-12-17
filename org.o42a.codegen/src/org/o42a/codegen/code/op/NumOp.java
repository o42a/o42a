/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.codegen.code.op;

import org.o42a.codegen.code.Code;


public interface NumOp<O extends NumOp<O>> extends Op {

	O neg(Code code);

	O add(Code code, O summand);

	O sub(Code code, O subtrahend);

	O mul(Code code, O multiplier);

	O div(Code code, O divisor);

	O rem(Code code, O divisor);

	BoolOp eq(Code code, O other);

	BoolOp ne(Code code, O other);

	BoolOp gt(Code code, O other);

	BoolOp ge(Code code, O other);

	BoolOp lt(Code code, O other);

	BoolOp le(Code code, O other);

	Int32op toInt32(Code code);

	Int64op toInt64(Code code);

	Fp64op toFp64(Code code);

	void returnValue(Code code);

}
