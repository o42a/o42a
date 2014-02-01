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
package org.o42a.parser.grammar.phrase;

import static org.o42a.parser.Grammar.DECLARATIVE;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.phrase.TypeDefinitionNode.Prefix;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class TypeDefinitionParser implements Parser<TypeDefinitionNode> {

	static final TypeDefinitionParser TYPE_DEFINITION =
			new TypeDefinitionParser();

	private TypeDefinitionParser() {
	}

	@Override
	public TypeDefinitionNode parse(ParserContext context) {
		if (context.next() != '#') {
			return null;
		}

		final SignNode<Prefix> prefix = parsePrefix(context);
		final ParenthesesNode definition =
				context.parse(DECLARATIVE.parentheses());

		if (definition == null) {
			return null;
		}

		return new TypeDefinitionNode(prefix, definition);
	}

	private SignNode<Prefix> parsePrefix(ParserContext context) {

		final SourcePosition start = context.current().fix();

		context.skip();

		return context.skipComments(
				false,
				new SignNode<>(
						start,
						context.current().fix(),
						TypeDefinitionNode.Prefix.HASH));
	}

}
