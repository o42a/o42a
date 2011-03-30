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
package org.o42a.parser.grammar.atom;

import static java.lang.Character.isLetter;
import static org.o42a.parser.Grammar.*;

import org.o42a.ast.EmptyNode;
import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.NameNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class NameParser implements Parser<NameNode> {

	public static final NameParser NAME = new NameParser();

	private static final WordParser WORD = new WordParser();
	private static final NumberParser NUMBER = new NumberParser();

	private NameParser() {
	}

	@Override
	public NameNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();
		final StringBuilder name = new StringBuilder();
		FixedPosition whitespace = null;
		int hyphen = 0;

		for (;;) {

			final int c = context.next();

			if (Character.getType(c) == Character.SPACE_SEPARATOR) {
				if (whitespace == null) {
					whitespace = context.current().fix();
				}
				context.push(whitespace(false));
				if (hyphen == NON_BREAKING_HYPHEN) {
					context.getLogger().discouragingWhitespace(
							new EmptyNode(whitespace, context.current()));
				}
				continue;
			}

			final int len = name.length();

			if (c == '-' || c == HYPHEN || c == NON_BREAKING_HYPHEN) {
				if (len == 0) {
					return null; // hyphen can not be first
				}
				if (hyphen != 0) {
					break; // two subsequent hyphens
				}
				if (whitespace != null) {
					// whitespace precedes hyphen
					if (c == '-') {
						break;
					}
					context.getLogger().discouragingWhitespace(
							new EmptyNode(whitespace, context.current()));
				}
				hyphen = c;
				continue;
			}
			if (c < 0) {
				break;
			}
			whitespace = null;

			final WordParser parser;

			if (isDigit(c)) {
				if (len == 0) {
					return null;// first symbol can not be a digit
				}
				parser = NUMBER;
			} else if (isLetter(c)) {
				parser = WORD;
			} else {
				break;
			}

			final CharSequence word = context.push(parser);

			if (word == null) {
				break;
			}
			if (len != 0) {
				if (hyphen != 0) {
					name.append('-');
					hyphen = 0;
				} else if (isDigit(name.charAt(name.length() - 1))) {
					if (parser == NUMBER) {
						// separate numbers by underscope
						name.append('_');
					}
				} else {
					if (parser != NUMBER) {
						// separate words by underscope
						name.append('_');
					}
				}
			}
			context.acceptAll();
			name.append(word);
			if (context.isEOF()) {
				break;
			}
		}

		if (name.length() != 0) {
			return new NameNode(
					start,
					context.firstUnaccepted(),
					name.toString());
		}

		return null;
	}

	private static class WordParser implements Parser<CharSequence> {

		@Override
		public CharSequence parse(ParserContext context) {

			final StringBuilder word = new StringBuilder();

			for (;;) {

				final int c = context.next();

				if (isNamePart(c)) {
					word.append(Character.toLowerCase((char) c));
					continue;
				}
				if (word.length() == 0) {
					return null;
				}
				context.acceptButLast();

				return word.length() > 0 ? word : null;
			}
		}

		protected boolean isNamePart(int c) {
			return isLetter(c);
		}

	}

	private static class NumberParser extends WordParser {

		@Override
		protected boolean isNamePart(int c) {
			return isDigit(c);
		}

	}

}
