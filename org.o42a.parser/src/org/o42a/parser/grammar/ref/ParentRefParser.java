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
package org.o42a.parser.grammar.ref;

import static org.o42a.parser.grammar.atom.NameParser.NAME;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.ParentRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class ParentRefParser implements Parser<ParentRefNode> {

	public static final ParentRefParser PARENT_REF = new ParentRefParser();

	private ParentRefParser() {
	}

	@Override
	public ParentRefNode parse(ParserContext context) {

		final NameNode name = context.push(NAME);

		if (name == null) {
			return null;
		}
		context.skipComments(false, name);
		if (context.next() != ':') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		if (context.next() != ':') {
			return null;
		}
		if (context.next() == '=') {
			return null; // definition
		}

		context.acceptButLast();

		final SignNode<ParentRefNode.Qualifier> qualifier = new SignNode<>(
				start,
				context.current().fix(),
				ParentRefNode.Qualifier.PARENT);

		return context.acceptComments(
				false,
				new ParentRefNode(name, qualifier));
	}

}
