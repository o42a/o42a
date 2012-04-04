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

	OBJ(0, false, false),
	LINK(1, false, false),
	VAR(2, true, false),
	GETTER(3, true, false),
	SCOPE(4, false, true),
	DEP(5, false, true),
	ASSIGNER(6, true, true);

	private final int code;
	private final boolean variable;
	private final boolean synthetic;

	FldKind(int code, boolean variable, boolean synthetic) {
		this.code = code;
		this.variable = variable;
		this.synthetic = synthetic;
	}

	public final int getCode() {
		return this.code;
	}

	public final boolean isVariable() {
		return this.variable;
	}

	public final boolean isSynthetic() {
		return this.synthetic;
	}

}
