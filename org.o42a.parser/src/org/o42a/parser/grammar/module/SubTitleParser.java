/*
    Parser
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.parser.grammar.module;

import static org.o42a.parser.Grammar.memberRef;
import static org.o42a.parser.Grammar.name;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.module.DoubleLine;
import org.o42a.ast.module.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class SubTitleParser implements Parser<SubTitleNode> {

	public static final SubTitleParser SUB_TITLE = new SubTitleParser();

	private static final DoubleLineParser DOUBLE_LINE = new DoubleLineParser();

	private SubTitleParser() {
	}

	@Override
	public SubTitleNode parse(ParserContext context) {

		final SignNode<DoubleLine> prefix = context.parse(DOUBLE_LINE);

		if (prefix == null) {
			return null;
		}

		final NameNode labelFirstName = context.parse(name());

		if (labelFirstName == null) {
			return new SubTitleNode(prefix, null, null);
		}

		final MemberRefNode owner =
				context.acceptComments(
						false,
						new MemberRefNode(
								null,
								null,
								labelFirstName,
								null,
								null));
		final MemberRefNode memberRef = context.parse(memberRef(owner));
		final MemberRefNode label = memberRef != null ? memberRef : owner;

		final SignNode<DoubleLine> suffix = context.parse(DOUBLE_LINE);

		return new SubTitleNode(prefix, label, suffix);
	}

	private static final class DoubleLineParser extends LineParser<DoubleLine> {

		DoubleLineParser() {
			super('=');
		}

		@Override
		protected DoubleLine createLine(int length) {
			return new DoubleLine(length);
		}

	}

}
