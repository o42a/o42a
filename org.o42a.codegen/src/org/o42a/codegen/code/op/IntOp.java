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

import org.o42a.codegen.code.Code;
import org.o42a.util.string.ID;


public interface IntOp<O extends IntOp<O>> extends NumOp<O> {

	ID SHL_ID = ID.rawId("shl");
	ID LSHR_ID = ID.rawId("lshr");
	ID ASHR_ID = ID.rawId("ashr");
	ID AND_ID = ID.rawId("and");
	ID OR_ID = ID.rawId("or");
	ID XOR_ID = ID.rawId("xor");
	ID COMP_ID = ID.rawId("comp");

	O shl(ID id, Code code, O numBits);

	O shl(ID id, Code code, int numBits);

	O lshr(ID id, Code code, O numBits);

	O lshr(ID id, Code code, int numBits);

	O ashr(ID id, Code code, O numBits);

	O ashr(ID id, Code code, int numBits);

	O and(ID id, Code code, O operand);

	O or(ID id, Code code, O operand);

	O xor(ID id, Code code, O operand);

	O comp(ID id, Code code);

	BoolOp lowestBit(ID id, Code code);

}
