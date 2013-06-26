/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

	OBJ(0),
	LINK(1),
	VAR(2),
	SCOPE(3),
	DEP(4),
	VAR_STATE(5),
	VOID_KEEPER(7),
	INTEGER_KEEPER(8),
	FLOAT_KEEPER(9),
	STRING_KEEPER(10),
	LINK_KEEPER(11),
	ARRAY_KEEPER(12),;

	private final int code;

	FldKind(int code) {
		this.code = code;
	}

	public final int code() {
		return this.code;
	}

}
