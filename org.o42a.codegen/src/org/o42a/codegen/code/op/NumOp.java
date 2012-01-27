/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;


public interface NumOp<O extends NumOp<O>> extends Op {

	O neg(CodeId id, Code code);

	O add(CodeId id, Code code, O summand);

	O sub(CodeId id, Code code, O subtrahend);

	O mul(CodeId id, Code code, O multiplier);

	O div(CodeId id, Code code, O divisor);

	O rem(CodeId id, Code code, O divisor);

	BoolOp eq(CodeId id, Code code, O other);

	BoolOp ne(CodeId id, Code code, O other);

	BoolOp gt(CodeId id, Code code, O other);

	BoolOp ge(CodeId id, Code code, O other);

	BoolOp lt(CodeId id, Code code, O other);

	BoolOp le(CodeId id, Code code, O other);

	Int8op toInt8(CodeId id, Code code);

	Int16op toInt16(CodeId id, Code code);

	Int32op toInt32(CodeId id, Code code);

	Int64op toInt64(CodeId id, Code code);

	Fp32op toFp32(CodeId id, Code code);

	Fp64op toFp64(CodeId id, Code code);

	void returnValue(Code code);

}
