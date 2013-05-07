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
package org.o42a.parser.grammar.type;

import static org.o42a.ast.atom.ParenthesisSign.CLOSING_PARENTHESIS;
import static org.o42a.ast.atom.ParenthesisSign.OPENING_PARENTHESIS;
import static org.o42a.parser.Grammar.type;

import java.util.ArrayList;

import org.o42a.ast.atom.*;
import org.o42a.ast.type.*;
import org.o42a.parser.Expectations;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


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
			return parseInterfaceWithType(context);
		}

		return null;
	}

	private InterfaceNode parseInterfaceWithType(ParserContext context) {

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<ParenthesisSign> opening = new SignNode<>(
				start,
				context.current().fix(),
				OPENING_PARENTHESIS);
		final SignNode<DefinitionKind> kind = context.push(DEFINITION_KIND);

		if (kind == null) {
			return null;
		}

		final ArrayList<TypeParameterNode> parameters = new ArrayList<>();
		SignNode<ParenthesisSign> closing = null;
		SignNode<CommaSign> separator = null;
		SeparatorNodes prevSeparators = null;

		final Expectations expectations =
				context.expectNothing().expect(',').expect(')');

		for (;;) {

			final TypeParameterNode parameter =
					expectations.parse(new TypeParameterParser(separator));

			if (parameter != null) {
				parameter.addComments(prevSeparators);
				prevSeparators = null;
				parameters.add(parameter);
			} else if (separator != null) {

				final TypeParameterNode emptyArg =
						new TypeParameterNode(separator.getEnd());

				parameters.add(emptyArg);
			}

			final SeparatorNodes separators = context.acceptComments(true);
			final int c = context.next();

			if (c == ')') {

				final SourcePosition closingStart = context.current().fix();

				context.acceptAll();

				closing = new SignNode<>(
						closingStart,
						context.current().fix(),
						CLOSING_PARENTHESIS);
				closing.addComments(prevSeparators);
				prevSeparators = null;
				closing.addComments(separators);
				break;
			}
			if (c != ',') {
				if (separators == null) {
					context.getLogger().notClosed(opening, "(");
					context.acceptButLast();
					break;
				}
				prevSeparators = separators;
				continue;
			}
			if (parameter == null && separator == null) {

				final TypeParameterNode emptyArg =
						new TypeParameterNode(opening.getEnd());

				emptyArg.addComments(separators);
				parameters.add(emptyArg);
			}

			final SourcePosition separatorStart = context.current().fix();

			context.acceptAll();
			separator = new SignNode<>(
					separatorStart,
					context.current().fix(),
					CommaSign.COMMA);
			separator.addComments(prevSeparators);
			prevSeparators = null;
			separator.addComments(separators);
			context.acceptComments(true, separator);
		}

		final InterfaceNode result = context.acceptComments(
				false,
				new InterfaceNode(
						opening,
						kind,
						parameters.toArray(
								new TypeParameterNode[parameters.size()]),
						closing));

		if (parameters.isEmpty()) {
			context.getLogger().error(
					"missing_type_parameters",
					result,
					"Type parameter not specified");
		}

		return result;
	}

	private static final class DefinitionKindParser
			implements Parser<SignNode<DefinitionKind>> {

		@Override
		public SignNode<DefinitionKind> parse(ParserContext context) {
			if (context.next() != '`') {
				return null;
			}

			final DefinitionKind targetKind;
			final SourcePosition start = context.current().fix();

			if (context.next() == '`') {
				targetKind = DefinitionKind.VARIABLE;
				context.acceptAll();
			} else {
				targetKind = DefinitionKind.LINK;
				context.acceptButLast();
			}

			final SignNode<DefinitionKind> result = new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					targetKind);

			return context.acceptComments(false, result);
		}

	}

	private static final class TypeParameterParser
			implements Parser<TypeParameterNode> {

		private final SignNode<CommaSign> separator;

		TypeParameterParser(SignNode<CommaSign> separator) {
			this.separator = separator;
		}

		@Override
		public TypeParameterNode parse(ParserContext context) {

			SourcePosition firstUnexpected = null;

			for (;;) {

				final SourcePosition start = context.current().fix();
				final TypeNode value = context.parse(type());

				if (value != null) {
					context.logUnexpected(firstUnexpected, start);
					context.acceptUnexpected();
					return new TypeParameterNode(this.separator, value);
				}

				// Unexpected input before the argument.
				// Possibly multiple lines.
				if (context.isEOF()) {
					if (firstUnexpected != null) {
						context.logUnexpected(firstUnexpected, start);
					} else {
						context.getLogger().eof(start);
					}
					return null;
				}
				if (!context.unexpected()) {
					context.logUnexpected(firstUnexpected, start);
					return null;
				}
				if (firstUnexpected == null) {
					firstUnexpected = start;
				}
				context.acceptAll();
				if (context.acceptComments(true) != null) {
					context.logUnexpected(firstUnexpected, start);
					firstUnexpected = null;
				}
			}
		}

	}

}
