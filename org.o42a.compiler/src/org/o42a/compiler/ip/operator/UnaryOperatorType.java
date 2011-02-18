/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import static org.o42a.core.ref.path.PathBuilder.pathBuilder;

import java.util.EnumMap;

import org.o42a.ast.expression.UnaryOperator;
import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.PathBuilder;


public enum UnaryOperatorType {

	PLUS(UnaryOperator.PLUS, "operators", "plus"),
	MINUS(UnaryOperator.MINUS, "operators", "minus");

	public static UnaryOperatorType byOperator(UnaryOperator operator) {

		final UnaryOperatorType result = Registry.operators.get(operator);

		assert result != null :
			"Unsupported unary operator: " + operator.getSign();

		return result;
	}

	private final UnaryOperator operator;
	private final PathBuilder path;

	UnaryOperatorType(UnaryOperator operator, String name, String... names) {
		this.operator = operator;
		this.path = pathBuilder(name, names);
		Registry.operators.put(operator, this);
	}

	public final UnaryOperator getOperator() {
		return this.operator;
	}

	public final PathBuilder getPath() {
		return this.path;
	}

	public Obj type(CompilerContext context) {
		return getPath().materialize(context);
	}

	@Override
	public String toString() {
		return this.operator.toString();
	}

	private static final class Registry {

		private static EnumMap<UnaryOperator, UnaryOperatorType> operators =
			new EnumMap<UnaryOperator, UnaryOperatorType>(UnaryOperator.class);
	}

}
