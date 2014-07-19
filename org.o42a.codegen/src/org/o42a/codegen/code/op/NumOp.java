/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.util.string.ID;


public interface NumOp<O extends NumOp<O>> extends Op {

	ID NEG_ID = ID.rawId("neg");
	ID ADD_ID = ID.rawId("add");
	ID SUB_ID = ID.rawId("sub");
	ID MUL_ID = ID.rawId("mul");
	ID DIV_ID = ID.rawId("div");
	ID REM_ID = ID.rawId("rem");
	ID GT_ID = ID.rawId("gt");
	ID GE_ID = ID.rawId("ge");
	ID LT_ID = ID.rawId("lt");
	ID LE_ID = ID.rawId("le");

	O neg(ID id, Code code);

	O add(ID id, Code code, O summand);

	O sub(ID id, Code code, O subtrahend);

	O mul(ID id, Code code, O multiplier);

	O div(ID id, Code code, O divisor);

	O rem(ID id, Code code, O divisor);

	BoolOp eq(ID id, Code code, O other);

	BoolOp ne(ID id, Code code, O other);

	BoolOp gt(ID id, Code code, O other);

	BoolOp ge(ID id, Code code, O other);

	BoolOp lt(ID id, Code code, O other);

	BoolOp le(ID id, Code code, O other);

	Int8op toInt8(ID id, Code code);

	Int16op toInt16(ID id, Code code);

	Int32op toInt32(ID id, Code code);

	Int64op toInt64(ID id, Code code);

	Fp32op toFp32(ID id, Code code);

	Fp64op toFp64(ID id, Code code);

	default void returnValue(Block code) {
		returnValue(code, true);
	}

	void returnValue(Block code, boolean dispose);

}
