/*
    Parser
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.parser.grammar.expression;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public abstract class AbstractBlockParser<
		B extends BlockNode<S>,
		S extends SignType>
				implements Parser<B> {

	private final S opening;
	private final S closing;

	public AbstractBlockParser(S opening, S closing) {
		this.opening = opening;
		this.closing = closing;
	}

	@Override
	public B parse(ParserContext context) {
		if (context.next() != this.opening.getSign().charAt(0)) {
			return null;
		}

		final FixedPosition start = context.current().fix();

		context.skip();

		final SignNode<S> opening =
			new SignNode<S>(start, context.current(), this.opening);
		SentenceNode[] content = context.parse(getContentParser());

		if (content == null) {
			content = new SentenceNode[0];
		}

		final B block;
		final int c = context.next();

		if (c == this.closing.getSign().charAt(0)) {

			final FixedPosition closingStart = context.current().fix();

			context.acceptAll();

			final SignNode<S> closing =
				new SignNode<S>(closingStart, context.current(), this.closing);

			block = createBlock(opening, content, closing);
			context.acceptComments(block);
		} else {
			context.getLogger().notClosed(
					start,
					this.opening.getSign());
			block = createBlock(opening, content, null);
		}

		return block;
	}

	protected abstract Parser<SentenceNode[]> getContentParser();

	protected abstract B createBlock(
			SignNode<S> opening,
			SentenceNode[] content,
			SignNode<S> closing);

}
