/*
    Parser
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.clause;

import static org.o42a.parser.Grammar.ref;
import static org.o42a.util.string.Characters.MINUS_SIGN;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class UnaryClauseIdParser implements Parser<UnaryNode> {

	static final UnaryClauseIdParser UNARY_CLAUSE_ID =
			new UnaryClauseIdParser();

	private UnaryClauseIdParser() {
	}

	@Override
	public UnaryNode parse(ParserContext context) {

		final UnaryOperator operator;

		switch (context.next()) {
		case '+':
			operator = UnaryOperator.PLUS;
			break;
		case '-':
		case MINUS_SIGN:
			operator = UnaryOperator.MINUS;
			break;
		default:
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<UnaryOperator> sign = context.acceptComments(
				false,
				new SignNode<>(start, context.current().fix(), operator));

		final RefNode operand = context.parse(ref());

		if (operand == null) {
			context.getLogger().missingOperand(sign, operator.getSign());
		}

		return new UnaryNode(sign, operand);
	}

}
