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
package org.o42a.parser;

import static java.util.Collections.emptyList;
import static org.o42a.parser.grammar.atom.CommentParser.COMMENT;

import java.util.ArrayList;
import java.util.List;

import org.o42a.ast.Node;
import org.o42a.ast.Position;
import org.o42a.ast.atom.CommentNode;


public abstract class ParserContext {

	public final <T> T parse(Parser<T> parser) {
		return parse(parser, getExpectations());
	}

	public final <T> T push(Parser<T> parser) {
		return push(parser, getExpectations());
	}

	public final void acceptAll() {
		acceptBut(0);
	}

	public final void acceptButLast() {
		acceptBut(isEOF() ? 0 : 1);
	}

	public abstract void acceptBut(int charsLeft);

	public abstract int next();

	public abstract void skip();

	public CommentNode[] skipComments() {

		final List<CommentNode> comments = parseComments();

		if (comments != null) {
			return comments.toArray(new CommentNode[comments.size()]);
		}

		return null;
	}

	public CommentNode[] acceptComments() {

		final CommentNode[] comments = skipComments();

		if (comments != null) {
			acceptAll();
		}

		return comments;
	}

	public final <T extends Node> T skipComments(T node) {

		final List<CommentNode> comments = parseComments();

		if (comments != null) {
			node.addComments(comments);
		}

		return node;
	}

	public final <T extends Node> T acceptComments(T node) {

		final List<CommentNode> comments = parseComments();

		if (comments != null) {
			acceptAll();
			node.addComments(comments);
		}

		return node;
	}

	public abstract Position current();

	public abstract Position firstUnaccepted();

	public abstract int unaccepted();

	public abstract int lastChar();

	public abstract boolean isEOF();

	public abstract ParserLogger getLogger();

	public abstract boolean hasErrors();

	public abstract Expectations getExpectations();

	public final Expectations expectNothing() {
		return new Expectations(this);
	}

	public final Expectations expect(Parser<?> expectation) {
		return getExpectations().expect(expectation);
	}

	public final Expectations expect(char expectedChar) {
		return getExpectations().expect(expectedChar);
	}

	public final boolean asExpected() {
		return getExpectations().asExpected(this);
	}

	protected abstract <T> T parse(Parser<T> parser, Expectations expectations);

	protected abstract <T> T push(Parser<T> parser, Expectations expectations);

	private List<CommentNode> parseComments() {
		if (isEOF()) {
			return null;
		}

		ArrayList<CommentNode> comments = null;
		final long start = current().offset();

		for (;;) {

			final CommentNode comment = push(COMMENT);

			if (comment == null) {
				break;
			}
			if (comments == null) {
				comments = new ArrayList<CommentNode>(1);
			}
			comments.add(comment);
		}
		if (comments != null) {
			return comments;
		}
		if (current().offset() == start) {
			return null;
		}

		return emptyList();
	}

}
