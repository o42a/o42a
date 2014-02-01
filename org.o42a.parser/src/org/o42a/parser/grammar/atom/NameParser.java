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
package org.o42a.parser.grammar.atom;

import static java.lang.Character.isLetter;
import static org.o42a.parser.Grammar.whitespace;
import static org.o42a.util.string.Characters.HYPHEN;
import static org.o42a.util.string.Characters.NON_BREAKING_HYPHEN;
import static org.o42a.util.string.Characters.isDigit;

import org.o42a.ast.atom.NameNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;
import org.o42a.util.string.NameBuilder;


public class NameParser implements Parser<NameNode> {

	public static final NameParser NAME = new NameParser();

	private static final WordParser WORD = new WordParser();
	private static final NumberParser NUMBER = new NumberParser();

	private NameParser() {
	}

	@Override
	public NameNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();
		final NameBuilder name = new NameBuilder();
		SourcePosition whitespace = null;
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
							new SourceRange(
									whitespace,
									context.current().fix()));
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
							new SourceRange(
									whitespace,
									context.current().fix()));
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

			final String word = context.push(parser);

			if (word == null) {
				break;
			}
			if (len != 0) {
				if (hyphen != 0) {
					name.append('-');
					hyphen = 0;
				} else if (isDigit(name.charAt(name.length() - 1))) {
					if (parser == NUMBER) {
						// Separate numbers by space.
						name.append(' ');
					}
				} else if (parser != NUMBER) {
					// Separate words by space.
					name.append(' ');
				}
			}

			context.acceptAll();
			name.append(word);
			if (context.isEOF()) {
				break;
			}
		}

		if (name.length() == 0) {
			return null;
		}

		return new NameNode(
				start,
				context.firstUnaccepted().fix(),
				name.toName());
	}

	private static class WordParser implements Parser<String> {

		@Override
		public String parse(ParserContext context) {

			final StringBuilder word = new StringBuilder();

			for (;;) {

				final int c = context.next();

				if (!isNamePart(c)) {
					break;
				}
				word.appendCodePoint(c);
			}

			if (word.length() == 0) {
				return null;
			}
			context.acceptButLast();

			if (word.length() == 0) {
				return null;
			}

			return word.toString();
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
