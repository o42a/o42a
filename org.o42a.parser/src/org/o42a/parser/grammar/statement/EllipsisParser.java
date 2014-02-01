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

import static org.o42a.parser.Grammar.IMPERATIVE;
import static org.o42a.util.string.Characters.HORIZONTAL_ELLIPSIS;

import org.o42a.ast.atom.HorizontalEllipsis;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.EllipsisNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class EllipsisParser implements Parser<EllipsisNode> {

	public static final EllipsisParser ELLIPSIS = new EllipsisParser();

	private EllipsisParser() {
	}

	@Override
	public EllipsisNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();

		switch (context.next()) {
		case HORIZONTAL_ELLIPSIS:
			break;
		case '.':
			if (context.next() != '.') {
				return null;
			}
			if (context.next() != '.') {
				return null;
			}
			break;
		default:
			return null;
		}

		context.acceptAll();

		final SignNode<HorizontalEllipsis> mark = new SignNode<>(
				start,
				context.current().fix(),
				HorizontalEllipsis.HORIZONTAL_ELLIPSIS);

		context.acceptComments(false, mark);

		return new EllipsisNode(mark, target(context));
	}

	private NameNode target(ParserContext context) {

		final StatementNode suffix = context.parse(IMPERATIVE.statement());

		if (suffix == null) {
			return null;
		}

		final RefNode ref = suffix.toRef();
		final MemberRefNode memberRef = ref == null ? null : ref.toMemberRef();

		if (memberRef == null) {
			context.getLogger().invalidEllipsisTarget(suffix);
			return null;
		}
		if (memberRef.getOwner() != null || memberRef.getDeclaredIn() != null) {
			context.getLogger().invalidEllipsisTarget(suffix);
			return null;
		}

		final NameNode name = memberRef.getName();

		if (name == null) {
			context.getLogger().invalidEllipsisTarget(suffix);
		}

		return name;
	}

}
