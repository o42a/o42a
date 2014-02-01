/*
    Parser
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.file;

import static org.o42a.parser.Grammar.name;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.file.AsteriskLine;
import org.o42a.ast.file.InclusionNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class InclusionParser implements Parser<InclusionNode> {

	public static final InclusionParser INCLUSION = new InclusionParser();

	private static final AsteriskLineParser PREFIX = new AsteriskLineParser(3);
	private static final AsteriskLineParser SUFFIX = new AsteriskLineParser(1);

	private InclusionParser() {
	}

	@Override
	public InclusionNode parse(ParserContext context) {

		final SignNode<AsteriskLine> prefix = context.parse(PREFIX);

		if (prefix == null) {
			return null;
		}

		final NameNode tag = context.parse(name());

		if (tag == null) {
			context.getLogger().missingInclusionTag(prefix);
			return new InclusionNode(prefix, null, null);
		}

		context.acceptComments(false, tag);

		final SignNode<AsteriskLine> suffix = context.parse(SUFFIX);

		return new InclusionNode(prefix, tag, suffix);
	}

	private static final class AsteriskLineParser
			extends LineParser<AsteriskLine> {

		AsteriskLineParser(int minLength) {
			super('*', minLength);
		}

		@Override
		protected AsteriskLine createLine(int length) {
			return new AsteriskLine(length);
		}

	}

}
