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
package org.o42a.parser.grammar.field;

import static org.o42a.parser.Grammar.DECLARATIVE;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode.Parenthesis;
import org.o42a.ast.field.InterfaceNode;
import org.o42a.ast.field.DefinitionKind;
import org.o42a.ast.ref.TypeNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class InterfaceParser implements Parser<InterfaceNode> {

	public static final InterfaceParser INTERFACE =
			new InterfaceParser();

	private static final DefinitionKindParser DEFINITION_KIND =
			new DefinitionKindParser();

	@Override
	public InterfaceNode parse(ParserContext context) {
		switch (context.next()) {
		case '`':

			final SignNode<DefinitionKind> kind =
					context.parse(DEFINITION_KIND);

			if (kind == null) {
				return null;
			}

			return new InterfaceNode(kind);
		case '(':
			return parseCast(context);
		}

		return null;
	}

	private InterfaceNode parseCast(ParserContext context) {

		final FixedPosition start = context.current().fix();

		context.skip();

		final SignNode<Parenthesis> opening = new SignNode<Parenthesis>(
				start,
				context.current(),
				Parenthesis.OPENING_PARENTHESIS);
		final SignNode<DefinitionKind> kind = context.push(DEFINITION_KIND);

		if (kind == null) {
			return null;
		}

		final TypeNode type;
		final ExpressionNode expression =
				context.parse(DECLARATIVE.expression());

		if (expression instanceof TypeNode) {
			type = (TypeNode) expression;
		} else if (expression == null) {
			context.getLogger().missingType(context.current());
			type = null;
		} else {
			context.getLogger().missingType(expression);
			type = null;
		}

		if (context.next() != ')') {
			context.getLogger().notClosed(opening, "(");
			return null;
		}

		final FixedPosition closingStart = context.current().fix();

		context.acceptAll();

		final SignNode<Parenthesis> closing = new SignNode<Parenthesis>(
				closingStart,
				context.current(),
				Parenthesis.CLOSING_PARENTHESIS);
		final InterfaceNode result =
				new InterfaceNode(opening, kind, type, closing);

		return context.acceptComments(false, result);
	}

	private static final class DefinitionKindParser
			implements Parser<SignNode<DefinitionKind>> {

		@Override
		public SignNode<DefinitionKind> parse(ParserContext context) {
			if (context.next() != '`') {
				return null;
			}

			final DefinitionKind targetKind;
			final FixedPosition start = context.current().fix();

			if (context.next() == '`') {
				targetKind = DefinitionKind.VARIABLE;
				context.acceptAll();
			} else {
				targetKind = DefinitionKind.LINK;
				context.acceptButLast();
			}

			final SignNode<DefinitionKind> result =
					new SignNode<DefinitionKind>(
							start,
							context.firstUnaccepted(),
							targetKind);

			return context.acceptComments(false, result);
		}

	}
}
