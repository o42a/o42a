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

import static org.o42a.parser.Grammar.separator;

import org.o42a.ast.Node;
import org.o42a.ast.Position;
import org.o42a.ast.atom.SeparatorNodes;


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

	public final SeparatorNodes skipComments(boolean allowNewLine) {
		return push(separator(allowNewLine));
	}

	public final SeparatorNodes acceptComments(boolean allowNewLine) {
		return parse(separator(allowNewLine));
	}

	public final <T extends Node> T skipComments(
			boolean allowNewLine,
			T node) {

		final SeparatorNodes separators = skipComments(allowNewLine);

		if (separators != null) {
			node.addComments(separators);
		}

		return node;
	}

	public final <T extends Node> T acceptComments(
			boolean allowNewLine,
			T node) {

		final SeparatorNodes separators = acceptComments(allowNewLine);

		if (separators != null) {
			node.addComments(separators);
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

	public final Expectations expect(String expectedString) {
		return getExpectations().expect(expectedString);
	}

	public final boolean asExpected() {
		return getExpectations().asExpected(this);
	}

	protected abstract <T> T parse(Parser<T> parser, Expectations expectations);

	protected abstract <T> T push(Parser<T> parser, Expectations expectations);

}
