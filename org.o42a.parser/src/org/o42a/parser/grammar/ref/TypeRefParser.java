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
package org.o42a.parser.grammar.ref;

import static org.o42a.ast.atom.ParenthesisSign.CLOSING_PARENTHESIS;
import static org.o42a.ast.atom.ParenthesisSign.OPENING_PARENTHESIS;
import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.TypeRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public abstract class TypeRefParser<
		T extends TypeRefNode<S>,
		S extends SignType>
				implements Parser<T> {

	private final Parser<SignNode<S>> prefixParser;

	public TypeRefParser(Parser<SignNode<S>> prefixParser) {
		this.prefixParser = prefixParser;
	}

	@Override
	public T parse(ParserContext context) {

		final SignNode<S> prefix = context.parse(this.prefixParser);

		if (prefix == null) {
			return null;
		}

		final SignNode<ParenthesisSign> opening = parseOpening(context);
		final RefNode type = parseType(context, prefix, opening);
		final SignNode<ParenthesisSign> closing =
				parseClosing(context, opening);

		return context.acceptComments(
				false,
				createNode(prefix, opening, type, closing));
	}

	protected abstract T createNode(
			SignNode<S> prefix,
			SignNode<ParenthesisSign> opening,
			RefNode type,
			SignNode<ParenthesisSign> closing);

	private SignNode<ParenthesisSign> parseOpening(ParserContext context) {

		final SignNode<ParenthesisSign> opening;

		if (context.next() != '(') {
			opening = null;
		} else {

			final SourcePosition start = context.current().fix();

			context.acceptAll();
			opening = context.acceptComments(
					true,
					new SignNode<>(
							start,
							context.current().fix(),
							OPENING_PARENTHESIS));
		}

		return opening;
	}

	private RefNode parseType(
			ParserContext context,
			SignNode<S> prefix,
			SignNode<ParenthesisSign> opening) {

		final RefNode declaredIn;

		if (opening != null) {
			declaredIn = context.expect(')').parse(ref());
		} else {
			declaredIn = context.parse(ref());
		}
		if (declaredIn == null) {
			context.getLogger().error(
					"missing_type",
					prefix,
					"Type reference is missing");
			return null;
		}

		return context.acceptComments(opening != null, declaredIn);
	}

	private SignNode<ParenthesisSign> parseClosing(
			ParserContext context,
			SignNode<ParenthesisSign> opening) {
		if (opening == null) {
			return null;
		}
		if (context.next() != ')') {
			context.getLogger().notClosed(opening, "(");
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		return new SignNode<>(
				start,
				context.current().fix(),
				CLOSING_PARENTHESIS);
	}

}
