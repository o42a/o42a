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
package org.o42a.parser.grammar.type;

import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.StaticRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class StaticRefParser implements Parser<StaticRefNode> {

	public static final StaticRefParser STATIC_REF = new StaticRefParser();

	public StaticRefParser() {
	}

	@Override
	public StaticRefNode parse(ParserContext context) {

		if (context.next() != '&') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<StaticRefNode.Prefix> prefix = new SignNode<>(
				start,
				context.current().fix(),
				StaticRefNode.Prefix.STATIC_REF);

		context.acceptComments(false, prefix);

		final RefNode ref = context.parse(ref());

		if (ref == null) {
			context.getLogger().error(
					"missing_static_ref",
					context.current(),
					"Static reference is missing");
		}

		return new StaticRefNode(prefix, ref);
	}

}
