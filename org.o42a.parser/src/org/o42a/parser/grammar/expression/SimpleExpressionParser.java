/*
    Parser
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import static org.o42a.util.string.Characters.MINUS_SIGN;
import static org.o42a.util.string.Characters.NOT_SIGN;
import static org.o42a.util.string.Characters.isDigit;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.GroupNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.type.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


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
		case MINUS_SIGN:

			final NumberNode number = context.parse(number());

			if (number != null) {
				return number;
			}

			return context.parse(unary());
		case '`':
		case '\\':
		case NOT_SIGN:
			return context.parse(unary());
		case '#':

			final RefNode macroRef = context.parse(ref());

			if (macroRef != null) {
				return macroRef;
			}

			return context.parse(macroExpansion());
		case '(':
			return context.parse(DECLARATIVE.parentheses());
		case '&':
			return context.parse(samples());
		case '"':
		case '\'':
			return context.parse(text());
		case '[':
			return context.parse(brackets());
		default:
			if (isDigit(c)) {
				return context.parse(number());
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
		int next = context.next();

		for (;;) {
			switch (next) {
			case '\\':

				final GroupNode group =
						context.parse(new GroupParser(expression));

				if (group == null) {
					return expression;
				}

				expression = group;
				next = context.pendingOrNext();

				continue;
			case '-':

				final DerefNode deref = context.parse(deref(expression));

				if (deref == null) {
					return expression;
				}

				final MemberRefNode derefMemberRef =
						context.parse(memberRef(deref, false));

				if (derefMemberRef != null) {
					expression = derefMemberRef;
				} else {
					expression = deref;
				}

				next = context.pendingOrNext();

				continue;
			}

			switch (next) {
			case '#':
			case ':':

				final MemberRefNode memberRef =
						context.parse(memberRef(expression, true));

				if (memberRef != null) {
					expression = memberRef;
					next = context.pendingOrNext();
					continue;
				}
				if (next == '#') {

					final PhraseNode phrase = context.parse(phrase(expression));

					if (phrase != null) {
						expression = phrase;
						next = context.pendingOrNext();
						continue;
					}
				}

				return expression;
			case '@':

				final AdapterRefNode adapterRef =
						context.parse(adapterRef(expression));

				if (adapterRef != null) {
					expression = adapterRef;
					next = context.pendingOrNext();
					continue;
				}

				return expression;
			case '`':

				final TypeNode argument =
						expression.toType() != null
						? expression.toType()
						: new MacroExpressionNode(expression);
				final TypeArgumentsNode typeArguments =
						context.parse(typeArguments(argument));

				if (typeArguments != null) {
					expression = typeArguments;
					next = context.pendingOrNext();
					continue;
				}

				return expression;
			default:
				if (next == '(') {

					final TypeNode type = expression.toType();

					if (type != null) {

						final TypeParametersNode typeParameters =
								context.parse(typeParameters(type));

						if (typeParameters != null) {
							expression = typeParameters;
							next = context.pendingOrNext();
							continue;
						}
					}
				}

				final PhraseNode phrase = context.parse(phrase(expression));

				if (phrase != null) {
					expression = phrase;
					next = context.pendingOrNext();
					continue;
				}

				return expression;
			}
		}
	}

}
