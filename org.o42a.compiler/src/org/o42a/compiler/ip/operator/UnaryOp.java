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

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.DefinitionValue;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;


public class UnaryOp extends Wrap {

	private final UnaryNode node;

	public UnaryOp(
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
	}

	public UnaryNode getNode() {
		return this.node;
	}

	@Override
	protected Ref resolveWrapped() {

		final Ref operand =
			getNode().getOperand().accept(EXPRESSION_VISITOR, distribute());
		final UnaryOperatorLookup operator =
			new UnaryOperatorLookup(operand, getNode());

		if (operator.getResolution().isFalse()) {
			// operator not supported
			return null;
		}

		return new DefinitionValue(
				new Location(getContext(), getNode().getSign()),
				distribute(),
				new AscendantsDefinition(
						operator,
						operator.distribute(),
						operator.toTypeRef()),
				emptyBlock(this));
	}

}
