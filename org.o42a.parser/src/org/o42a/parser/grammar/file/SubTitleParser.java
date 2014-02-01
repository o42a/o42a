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

import static org.o42a.parser.Grammar.memberRef;
import static org.o42a.parser.Grammar.name;
import static org.o42a.parser.grammar.file.OddParser.ODD;

import org.o42a.ast.Node;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.file.DoubleLine;
import org.o42a.ast.file.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class SubTitleParser implements Parser<SubTitleNode> {

	public static final SubTitleParser SUB_TITLE = new SubTitleParser();

	private static final DoubleLineParser PREFIX = new DoubleLineParser(3);
	private static final DoubleLineParser SUFFIX = new DoubleLineParser(1);

	private SubTitleParser() {
	}

	@Override
	public SubTitleNode parse(ParserContext context) {

		final SeparatorNodes comments = context.skipComments(true);
		final SignNode<DoubleLine> prefix = parsePrefix(context);

		if (prefix == null) {
			return null;
		}

		final NameNode tagFirstName = context.parse(name());

		if (tagFirstName == null) {
			context.parse(ODD);
			return addComments(new SubTitleNode(prefix, null, null), comments);
		}

		final MemberRefNode owner = context.acceptComments(
					false,
					addComments(
							new MemberRefNode(null, null, tagFirstName, null),
							comments));
		final MemberRefNode memberRef = context.parse(memberRef(owner, true));
		final MemberRefNode tag = memberRef != null ? memberRef : owner;

		final SignNode<DoubleLine> suffix = context.parse(SUFFIX);

		context.parse(ODD);

		return new SubTitleNode(prefix, tag, suffix);
	}

	private SignNode<DoubleLine> parsePrefix(ParserContext context) {

		final boolean lineStart = context.isLineStart();
		final SignNode<DoubleLine> prefix = context.parse(PREFIX);

		if (prefix != null && !lineStart) {
			invalidTitleUnderline(context, prefix);
		}

		return prefix;
	}

	private <N extends Node> N addComments(N node, SeparatorNodes comments) {
		if (comments == null) {
			return node;
		}
		node.addComments(comments.getComments());
		return node;
	}

	private void invalidTitleUnderline(
			ParserContext context,
			SignNode<DoubleLine> titleLine) {
		context.getLogger().error(
				"invalid_title_underline",
				titleLine,
				"Nothing but spaces may preceed the title underline");
	}

	private static final class DoubleLineParser extends LineParser<DoubleLine> {

		DoubleLineParser(int minLength) {
			super('=', minLength);
		}

		@Override
		protected DoubleLine createLine(int length) {
			return new DoubleLine(length);
		}

	}

}
