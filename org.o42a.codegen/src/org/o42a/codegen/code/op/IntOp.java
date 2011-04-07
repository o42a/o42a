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


public interface IntOp<O extends IntOp<O>> extends NumOp<O> {

	O shl(String name, Code code, O numBits);

	O shl(String name, Code code, int numBits);

	O lshr(String name, Code code, O numBits);

	O lshr(String name, Code code, int numBits);

	O ashr(String name, Code code, O numBits);

	O ashr(String name, Code code, int numBits);

	O and(String name, Code code, O operand);

	O or(String name, Code code, O operand);

	O xor(String name, Code code, O operand);

	BoolOp lowestBit(String name, Code code);

}
