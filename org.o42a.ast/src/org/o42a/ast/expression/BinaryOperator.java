/*
    Abstract Syntax Tree
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
package org.o42a.ast.expression;

import org.o42a.ast.atom.SignType;


public enum BinaryOperator implements SignType {

	GREATER(">", -4),
	GREATER_OR_EQUAL(">=", -4),
	LESS("<", -4),
	LESS_OR_EQUAL("<=", -4),
	NOT_EQUAL("<>", -4),
	EQUAL("==", -4),
	COMPARE("<=>", -3),
	ADD("+", -2),
	SUBTRACT("-", -2),
	MULTIPLY("*", -1),
	DIVIDE("/", -1),
	SUFFIX("~", 0);

	private final String sign;
	private final int priority;

	BinaryOperator(String sign, int priority) {
		this.sign = sign;
		this.priority = priority;
	}

	public final boolean isArithmetic() {
		return ordinal() >= ADD.ordinal();
	}

	public final boolean isEquality() {
		return this == NOT_EQUAL || this == EQUAL;
	}

	public final boolean isCompare() {
		return this == COMPARE;
	}

	public final boolean isComparison() {
		return ordinal() <= LESS_OR_EQUAL.ordinal();
	}

	public final boolean isSuffix() {
		return this == SUFFIX;
	}

	/**
	 * Whether this operator is always left-associative.
	 *
	 * @return <code>true</code> if this operator always has a higher priority
	 * than a following one, or <code>false</code> if operator's priorities
	 * priorities are determined according to their {@link #getPriority()
	 * priority values}.</p>
	 */
	public final boolean alwaysLeftAssociative() {
		return this.priority >= 0;
	}

	/**
	 * Binary operator's priority.
	 *
	 * <p>The bigger this number, the higher priority this operator has.</p>
	 *
	 * <p>The priority values are typically negative. The non-negative value
	 * means the operator is {@link #alwaysLeftAssociative() always
	 * left-associative}.</p>
	 *
	 * @return priority value.
	 */
	public final int getPriority() {
		return this.priority;
	}

	@Override
	public String getSign() {
		return this.sign;
	}

	@Override
	public String toString() {
		return this.sign;
	}

}
