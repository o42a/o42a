/*
    Parser
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MetaRefNode;
import org.o42a.ast.ref.MetaRefNode.Prefix;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MetaRefParser implements Parser<MetaRefNode> {

	public static final MetaRefParser META_REF = new MetaRefParser();

	private MetaRefParser() {
	}

	@Override
	public MetaRefNode parse(ParserContext context) {
		if (context.next() != '#') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<Prefix> prefix = context.acceptComments(
				false,
				new SignNode<MetaRefNode.Prefix>(
						start,
						context.current().fix(),
						MetaRefNode.Prefix.HASH));

		final RefNode macro = context.parse(ref());

		if (macro == null) {
			context.getLogger().error(
					"missing_macro",
					prefix,
					"Missing macro reference to substitute");
		}

		return new MetaRefNode(prefix, macro);
	}

}
