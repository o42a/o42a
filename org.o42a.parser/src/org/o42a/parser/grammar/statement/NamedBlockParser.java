/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.braces;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.statement.NamedBlockNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class NamedBlockParser implements Parser<NamedBlockNode> {

	private final NameNode name;

	public NamedBlockParser(NameNode name) {
		this.name = name;
	}

	@Override
	public NamedBlockNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();

		if (context.next() != ':') {
			return null;
		}

		context.skip();

		final SignNode<NamedBlockNode.Separator> separator = new SignNode<>(
				start,
				context.current().fix(),
				NamedBlockNode.Separator.COLON);

		context.skipComments(true, separator);

		final BracesNode block = context.parse(braces());

		if (block == null) {
			return null;
		}

		return new NamedBlockNode(this.name, separator, block);
	}

}
