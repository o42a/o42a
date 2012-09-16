/*
    Compiler Core
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
package org.o42a.core.ir.field;


public enum FldKind {

	OBJ(0, false),
	LINK(1, true),
	VAR(2, true),
	SCOPE(3, false),
	DEP(4, false),
	ASSIGNER(5, true),
	VOID_KEEPER(6, false),
	INTEGER_KEEPER(7, false),
	FLOAT_KEEPER(8, false),
	STRING_KEEPER(9, false),
	LINK_KEEPER(10, false),
	ARRAY_KEEPER(11, false),;

	private final int code;
	private final boolean variable;

	FldKind(int code, boolean variable) {
		this.code = code;
		this.variable = variable;
	}

	public final int code() {
		return this.code;
	}

	public final boolean isVariable() {
		return this.variable;
	}

}
