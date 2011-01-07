/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.core.CompilerContext;
import org.o42a.core.Location;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.AdapterLookup;


final class BinaryOperatorLookup extends AdapterLookup {

	private final BinaryNode node;

	private static StaticTypeRef adapterType(
			CompilerContext context,
			BinaryOperator operator) {
		switch (operator) {
		case ADD:
			return absolutePath(context, "operators", "add")
			.target(context).toStaticTypeRef();
		case SUBTRACT:
			return absolutePath(context, "operators", "subtract")
			.target(context).toStaticTypeRef();
		case MULTIPLY:
			return absolutePath(context, "operators", "multiply")
			.target(context).toStaticTypeRef();
		case DIVIDE:
			return absolutePath(context, "operators", "divide")
			.target(context).toStaticTypeRef();
		case EQUAL:
		case NOT_EQUAL:
			return absolutePath(context, "operators", "equals")
			.target(context).toStaticTypeRef();
		case LESS:
		case LESS_OR_EQUAL:
		case GREATER:
		case GREATER_OR_EQUAL:
			return absolutePath(context, "operators", "compare")
			.target(context).toStaticTypeRef();
		}

		throw new IllegalStateException(
				"Unrecognized binary operator: " + operator);
	}

	private static StaticTypeRef secondAdapterType(
			CompilerContext context,
			BinaryOperator operator) {
		switch (operator) {
		case EQUAL:
		case NOT_EQUAL:
			return absolutePath(context, "operators", "compare")
			.target(context).toStaticTypeRef();
		default:
			return null;
		}
	}

	BinaryOperatorLookup(Ref operand, BinaryNode node) {
		super(
				new Location(operand.getContext(), node),
				operand,
				adapterType(operand.getContext(), node.getOperator()),
				secondAdapterType(operand.getContext(), node.getOperator()));
		this.node = node;
	}

	public BinaryNode getNode() {
		return this.node;
	}

	@Override
	protected void noAdapter(Ref owner) {
		getLogger().unsupportedBinaryOperator(
				getNode().getSign(),
				getNode().getOperator().getSign(),
				getAdapterType());
	}

}
