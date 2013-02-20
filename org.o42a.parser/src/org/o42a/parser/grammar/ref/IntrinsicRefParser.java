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
package org.o42a.parser.grammar.ref;

import static org.o42a.parser.grammar.atom.NameParser.NAME;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class IntrinsicRefParser implements Parser<IntrinsicRefNode> {

	public static final IntrinsicRefParser INTRINSIC_REF =
			new IntrinsicRefParser();

	private IntrinsicRefParser() {
	}

	@Override
	public IntrinsicRefNode parse(ParserContext context) {
		if (context.next() != '$') {
			return null;
		}

		final SourcePosition prefixStart = context.current().fix();

		context.skip();

		final SignNode<IntrinsicRefNode.Boundary> prefix = new SignNode<>(
				prefixStart,
				context.current().fix(),
				IntrinsicRefNode.Boundary.DOLLAR);

		context.skipComments(false, prefix);

		final NameNode name = context.push(NAME);

		if (name == null) {
			return null;
		}
		context.skipComments(false, name);
		if (context.next() != '$') {
			return null;
		}

		final SourcePosition suffixStart = context.current().fix();

		context.acceptAll();

		final SignNode<IntrinsicRefNode.Boundary> suffix = new SignNode<>(
				suffixStart,
				context.current().fix(),
				IntrinsicRefNode.Boundary.DOLLAR);

		return context.acceptComments(
				false,
				new IntrinsicRefNode(prefix, name, suffix));
	}

}
