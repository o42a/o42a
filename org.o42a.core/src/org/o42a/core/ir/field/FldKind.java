/*
    Compiler Core
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
package org.o42a.core.ir.field;


public enum FldKind {

	OBJ(0, false),
	LINK(1, false),
	VAR(2, false),
	ARRAY(3, false),
	SCOPE(4, true),
	DEP(5, true);

	private final int code;
	private final boolean synthetic;

	FldKind(int code, boolean generated) {
		this.code = code;
		this.synthetic = generated;
	}

	public final int getCode() {
		return this.code;
	}

	public final boolean isSynthetic() {
		return this.synthetic;
	}

}
