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

import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.DeclarableAdapterNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class DeclarableAdapterParser implements Parser<DeclarableAdapterNode> {

	public static final DeclarableAdapterParser DECLARABLE_ADAPTER =
		new DeclarableAdapterParser();

	private DeclarableAdapterParser() {
	}

	@Override
	public DeclarableAdapterNode parse(ParserContext context) {
		if (context.next() != '@') {
			return null;
		}

		final FixedPosition start = context.current().fix();

		context.skip();

		final SignNode<DeclarableAdapterNode.Prefix> prefix =
			new SignNode<DeclarableAdapterNode.Prefix>(
					start,
					context.current(),
					DeclarableAdapterNode.Prefix.ADAPTER);

		context.skipComments(false, prefix);

		final RefNode ref = context.push(ref());

		if (!(ref instanceof MemberRefNode)) {
			return null;
		}

		context.acceptAll();

		return new DeclarableAdapterNode(prefix, (MemberRefNode) ref);
	}

}
