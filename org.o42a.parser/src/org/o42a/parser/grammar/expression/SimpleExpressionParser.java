/*
    Parser
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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.AdapterRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class SimpleExpressionParser implements Parser<ExpressionNode> {

	private final Grammar grammar;

	public SimpleExpressionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public ExpressionNode parse(ParserContext context) {

		ExpressionNode expression = base(context);

		if (expression == null) {
			return null;
		}

		for (;;) {
			switch (context.next()) {
			case ':':

				final MemberRefNode fieldRef =
					context.parse(memberRef(expression, true));

				if (fieldRef != null) {
					expression = fieldRef;
					continue;
				}

				return expression;
			case '@':

				final AdapterRefNode adapterRef =
					context.parse(adapterRef(expression));

				if (adapterRef != null) {
					expression = adapterRef;
					continue;
				}

				return expression;
			default:

				final PhraseNode phrase =
					context.parse(this.grammar.phrase(expression));

				if (phrase != null) {
					expression = phrase;
					continue;
				}

				return expression;
			}
		}
	}

	private ExpressionNode base(ParserContext context) {

		final int c = context.next();

		switch (c) {
		case '+':
		case '-':
			return context.parse(this.grammar.unaryExpression());
		case '(':
			return context.parse(this.grammar.parentheses());
		case '&':
			return context.parse(samples());
		case '"':
		case '\'':
		case '\\':
			return context.parse(text());
		default:
			if (Grammar.isDigit(c)) {
				return context.parse(decimal());
			}

			final RefNode ref = context.parse(ref());

			if (ref == null) {
				return null;
			}

			final AscendantsNode ascendants = context.parse(ascendants(ref));

			if (ascendants != null) {
				return ascendants;
			}

			return ref;
		}
	}

}
