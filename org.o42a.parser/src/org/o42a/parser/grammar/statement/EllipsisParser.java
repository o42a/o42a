/*
    Parser
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.HORIZONTAL_ELLIPSIS;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.EllipsisNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class EllipsisParser implements Parser<EllipsisNode> {

	public static final EllipsisParser ELLIPSIS = new EllipsisParser();

	private EllipsisParser() {
	}

	@Override
	public EllipsisNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();

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

		final SignNode<EllipsisNode.Mark> mark =
			new SignNode<EllipsisNode.Mark>(
					start,
					context.current(),
					EllipsisNode.Mark.ELLIPSIS);

		context.acceptComments(false, mark);

		final NameNode target;
		final StatementNode suffix =
			context.parse(IMPERATIVE.statement());

		if (suffix == null) {
			target = null;
		} else if (!(suffix instanceof MemberRefNode)) {
			context.getLogger().invalidEllipsisTarget(suffix);
			target = null;
		} else {

			final MemberRefNode ref = (MemberRefNode) suffix;

			if (ref.getOwner() != null || ref.getDeclaredIn() != null) {
				context.getLogger().invalidEllipsisTarget(suffix);
				target = null;
			} else {
				target = ref.getName();
				if (target == null) {
					context.getLogger().invalidEllipsisTarget(suffix);
				}
			}
		}

		return new EllipsisNode(mark, target);
	}

}
