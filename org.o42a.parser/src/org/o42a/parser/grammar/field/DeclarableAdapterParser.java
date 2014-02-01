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
package org.o42a.parser.grammar.field;

import static org.o42a.ast.ref.MembershipSign.DECLARED_IN;
import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MembershipSign;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


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

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<MembershipSign> prefix = new SignNode<>(
				start,
				context.current().fix(),
				DECLARED_IN);

		context.skipComments(false, prefix);

		final RefNode ref = context.push(ref());

		if (ref == null) {
			return null;
		}

		final MemberRefNode memberRef = ref.toMemberRef();

		if (memberRef == null) {
			return null;
		}

		context.acceptAll();

		return new DeclarableAdapterNode(prefix, memberRef);
	}

}
