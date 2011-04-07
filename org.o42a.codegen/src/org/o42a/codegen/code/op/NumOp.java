/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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

	O neg(String name, Code code);

	O add(String name, Code code, O summand);

	O sub(String name, Code code, O subtrahend);

	O mul(String name, Code code, O multiplier);

	O div(String name, Code code, O divisor);

	O rem(String name, Code code, O divisor);

	BoolOp eq(String name, Code code, O other);

	BoolOp ne(String name, Code code, O other);

	BoolOp gt(String name, Code code, O other);

	BoolOp ge(String name, Code code, O other);

	BoolOp lt(String name, Code code, O other);

	BoolOp le(String name, Code code, O other);

	Int8op toInt8(String name, Code code);

	Int16op toInt16(String name, Code code);

	Int32op toInt32(String name, Code code);

	Int64op toInt64(String name, Code code);

	Fp32op toFp32(String name, Code code);

	Fp64op toFp64(String name, Code code);

	void returnValue(Code code);

}
