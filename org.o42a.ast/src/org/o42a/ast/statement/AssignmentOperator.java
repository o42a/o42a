/*
    Abstract Syntax Tree
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
package org.o42a.ast.statement;

import org.o42a.ast.atom.SignType;


public enum AssignmentOperator implements SignType {

	BIND("<-"),
	ASSIGN("<<"),
	ADD_AND_ASSIGN("+<<"),
	SUBTRACT_AND_ASSIGN("-<<"),
	MULTIPLY_AND_ASSIGN("*<<"),
	DIVIDE_AND_ASSIGN("/<<");

	private final String sign;

	AssignmentOperator(String sign) {
		this.sign = sign;
	}

	public final boolean isBinding() {
		return this == BIND;
	}

	public final boolean isCombined() {
		return ordinal() >= ADD_AND_ASSIGN.ordinal();
	}

	@Override
	public String getSign() {
		return this.sign;
	}

}
