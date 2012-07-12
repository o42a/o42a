/*
    Parser
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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.*;
import static org.o42a.util.string.Characters.MINUS;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.ast.type.ValueTypeNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.parser.grammar.type.ValueTypeParser;


public class SimpleExpressionParser implements Parser<ExpressionNode> {

	public static final SimpleExpressionParser SIMPLE_EXPRESSION =
			new SimpleExpressionParser();

	private final ExpressionNode base;

	private SimpleExpressionParser() {
		this.base = null;
	}

	public SimpleExpressionParser(ExpressionNode base) {
		this.base = base;
	}

	@Override
	public ExpressionNode parse(ParserContext context) {

		ExpressionNode result;

		if (this.base != null) {
			result = parse(context, this.base);
			if (result == this.base) {
				return null;
			}
		} else {

			final ExpressionNode base = base(context);

			if (base == null) {
				return null;
			}
			result = parse(context, base);
		}

		return result;
	}

	private ExpressionNode base(ParserContext context) {
		if (this.base != null) {
			return this.base;
		}

		final int c = context.next();

		switch (c) {
		case '+':
		case '-':
		case MINUS:
			return context.parse(unaryExpression());
		case '(':
			return context.parse(DECLARATIVE.parentheses());
		case '&':
			return context.parse(samples());
		case '"':
		case '\'':
		case '\\':
			return context.parse(text());
		case '[':
			return context.parse(brackets());
		case '#':
			return context.parse(metaExpression());
		default:
			if (isDigit(c)) {
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

	private ExpressionNode parse(ParserContext context, ExpressionNode base) {

		ExpressionNode expression = base;

		for (;;) {

			final int c = context.next();
			final int next;

			switch (c) {
			case '`':

				final BodyRefNode bodyRef = context.parse(bodyRef(expression));

				if (bodyRef == null) {
					return expression;
				}

				final MemberRefNode bodyMemberRef =
						context.parse(memberRef(bodyRef, false));

				if (bodyMemberRef != null) {
					expression = bodyMemberRef;
					continue;
				}

				expression = bodyRef;
				next = context.next();

				break;
			case '-':

				final DerefNode deref = context.parse(deref(expression));

				if (deref == null) {
					return expression;
				}

				final MemberRefNode derefMemberRef =
						context.parse(memberRef(deref, false));

				if (derefMemberRef != null) {
					expression = derefMemberRef;
					continue;
				}

				expression = deref;
				next = context.next();

				break;
			default:
				next = c;
			}

			switch (next) {
			case ':':

				final MemberRefNode memberRef =
						context.parse(memberRef(expression, true));

				if (memberRef != null) {
					expression = memberRef;
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
				if (next == '(' && expression instanceof TypeNode) {

					final TypeNode ascendant = (TypeNode) expression;
					final ValueTypeNode valueType =
							context.parse(new ValueTypeParser(ascendant));

					if (valueType != null) {
						expression = valueType;
						continue;
					}
				}

				final PhraseNode phrase = context.parse(phrase(expression));

				if (phrase != null) {
					expression = phrase;
					continue;
				}

				return expression;
			}
		}
	}

}
