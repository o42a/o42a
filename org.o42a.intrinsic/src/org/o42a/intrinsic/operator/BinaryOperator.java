/*
    Intrinsics
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.intrinsic.operator;

import static org.o42a.core.ref.path.PathBuilder.pathBuilder;

import org.o42a.core.ref.path.PathBuilder;


public enum BinaryOperator {

	ADD("+", pathBuilder("operators", "add")),
	SUBTRACT("-", pathBuilder("operators", "subtract")),
	MULTIPLY("*", pathBuilder("operators", "multiply")),
	DIVIDE("/", pathBuilder("operators", "divide")),
	EQUALS(
			"==",
			pathBuilder("operators", "equals"),
			pathBuilder("operators", "equals", "to")),
	COMPARE(
			"<compare>",
			pathBuilder("operators", "compare"),
			pathBuilder("operators", "compare", "with"));

	private final String sign;
	private final PathBuilder path;
	private final PathBuilder rightOperand;

	BinaryOperator(String sign, PathBuilder path) {
		this.sign = sign;
		this.path = path;
		this.rightOperand = Constants.RIGHT_OPERANT;
	}

	BinaryOperator(
			String sign,
			PathBuilder path,
			PathBuilder rightOperand) {
		this.sign = sign;
		this.path = path;
		this.rightOperand = rightOperand;
	}

	public final String getSign() {
		return this.sign;
	}

	public final PathBuilder getPath() {
		return this.path;
	}

	public final PathBuilder getRightOperand() {
		return this.rightOperand;
	}

	private static final class Constants {

		static final PathBuilder RIGHT_OPERANT =
			pathBuilder("operators", "binary_operator", "right_operand");

	}

}
