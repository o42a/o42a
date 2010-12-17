/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.core.CompilerContext;
import org.o42a.core.Location;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.AdapterLookup;


public class UnaryOperatorLookup extends AdapterLookup {

	private static StaticTypeRef adapterType(
			CompilerContext context,
			UnaryNode node) {
		switch (node.getOperator()) {
		case PLUS:
			return absolutePath(context, "operators", "plus")
			.target(context).toStaticTypeRef();
		case MINUS:
			return absolutePath(context, "operators", "minus")
			.target(context).toStaticTypeRef();
		default:
			throw new IllegalStateException(
					"Unrecognized unary operator: " + node.getOperator());
		}
	}

	public UnaryOperatorLookup(Ref operand, UnaryNode node) {
		super(
				new Location(operand.getContext(), node),
				operand,
				adapterType(operand.getContext(), node));
	}

	@Override
	public UnaryNode getNode() {
		return (UnaryNode) super.getNode();
	}

	@Override
	protected void noAdapter(Ref owner) {
		getLogger().unsupportedUnaryOperator(
				getNode().getSign(),
				getNode().getOperator().getSign(),
				getAdapterType());
	}

}
