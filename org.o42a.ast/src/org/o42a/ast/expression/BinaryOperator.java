/*
    Abstract Syntax Tree
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
package org.o42a.ast.expression;

import org.o42a.ast.atom.SignType;


public enum BinaryOperator implements SignType {

	GREATER(">", 0),
	GREATER_OR_EQUAL(">=", 0),
	LESS("<", 0),
	LESS_OR_EQUAL("<=", 0),
	NOT_EQUAL("<>", 0),
	EQUAL("==", 0),
	ADD("+", 1),
	SUBTRACT("-", 1),
	MULTIPLY("*", 2),
	DIVIDE("/", 2);

	private final String sign;
	private final int priority;

	BinaryOperator(String sign, int priority) {
		this.sign = sign;
		this.priority = priority;
	}

	@Override
	public String getSign() {
		return this.sign;
	}

	public int getPriority() {
		return this.priority;
	}

	@Override
	public String toString() {
		return this.sign;
	}

}
