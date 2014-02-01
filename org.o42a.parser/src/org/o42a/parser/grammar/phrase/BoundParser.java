/*
    Parser
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.phrase;

import static org.o42a.parser.Grammar.expression;
import static org.o42a.parser.Grammar.simpleExpression;
import static org.o42a.util.string.Characters.INFINITY;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.phrase.NoBoundNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class BoundParser implements Parser<BoundNode> {

	static final BoundParser BOUND = new BoundParser();

	private static final MinusParser MINUS = new MinusParser();

	private BoundParser() {
	}

	@Override
	public BoundNode parse(ParserContext context) {
		if (context.next() == INFINITY) {
			return infinity(context);
		}
		return noBoundOrMinus(context);
	}

	private static NoBoundNode infinity(ParserContext context) {

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		return context.acceptComments(
				true,
				new NoBoundNode(start, context.current().fix()));
	}

	private static BoundNode noBoundOrMinus(ParserContext context) {

		final NoBoundNode minus = context.parse(MINUS);
		final BoundNode bound;

		if (minus == null) {
			bound = context.parse(expression());
			if (bound == null) {
				return null;
			}
		} else {

			final ExpressionNode operand = context.parse(simpleExpression());

			if (operand == null) {
				bound = minus;
			} else {

				final SignNode<UnaryOperator> sign = new SignNode<>(
						minus.getStart(),
						minus.getEnd(),
						UnaryOperator.MINUS);

				sign.addComments(minus.getComments());

				bound = new UnaryNode(sign, operand);
			}
		}

		return context.acceptComments(true, bound);
	}

	private static final class MinusParser implements Parser<NoBoundNode> {

		@Override
		public NoBoundNode parse(ParserContext context) {
			if (context.next() != '-') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			if (context.next() == '-') {
				return null;
			}

			context.acceptButLast();

			return context.acceptComments(
					false,
					new NoBoundNode(start, context.firstUnaccepted().fix()));
		}

	}

}
