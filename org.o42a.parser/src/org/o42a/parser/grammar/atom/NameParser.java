/*
    Parser
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static java.lang.Character.isUpperCase;
import static org.o42a.parser.Grammar.isDigit;
import static org.o42a.parser.Grammar.whitespace;
import static org.o42a.util.string.Characters.HYPHEN;
import static org.o42a.util.string.Characters.NON_BREAKING_HYPHEN;

import org.o42a.ast.atom.NameNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;
import org.o42a.util.string.Capitalization;


public class NameParser implements Parser<NameNode> {

	public static final NameParser NAME = new NameParser();

	private static final WordParser WORD = new WordParser();
	private static final NumberParser NUMBER = new NumberParser();

	private NameParser() {
	}

	@Override
	public NameNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();
		final StringBuilder name = new StringBuilder();
		SourcePosition whitespace = null;
		int hyphen = 0;
		Capitalization capitalization = Capitalization.CASE_INSENSITIVE;

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

			final Word word = context.push(parser);

			if (word == null) {
				break;
			}
			if (len == 0) {
				if (word.notFirstCapital()) {
					// Preserve capital if the first word contains
					// a capital letter not at the beginning.
					// This is for abbreviations like "URL".
					capitalization = Capitalization.PRESERVE_CAPITAL;
				}
			} else {
				if (word.firstCapital() && !word.notFirstCapital()) {
					// Preserve capital if a not first word starts with
					// a capital letter and has no more capitals.
					// This is for proper nouns like "John Smith".
					capitalization = Capitalization.PRESERVE_CAPITAL;
				}
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
			name.append(word.getWord());
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
				capitalization.name(name.toString()));
	}

	private static final class Word {

		private final CharSequence word;
		private final boolean notFirstCapital;
		private final boolean firstCapital;

		Word(CharSequence word, boolean firstCapital, boolean notFirstCapital) {
			this.word = word;
			this.firstCapital = firstCapital;
			this.notFirstCapital = notFirstCapital;
		}

		public final CharSequence getWord() {
			return this.word;
		}

		public final boolean firstCapital() {
			return this.firstCapital;
		}

		public final boolean notFirstCapital() {
			return this.notFirstCapital;
		}

		@Override
		public String toString() {
			return String.valueOf(this.word);
		}

	}

	private static class WordParser implements Parser<Word> {

		@Override
		public Word parse(ParserContext context) {

			final StringBuilder word = new StringBuilder();
			boolean firstCapital = false;
			boolean notFirstCapital = false;

			for (;;) {

				final int c = context.next();

				if (!isNamePart(c)) {
					break;
				}
				if (word.length() != 0) {
					firstCapital |= isUpperCase(c);
				} else {
					notFirstCapital |= isUpperCase(c);
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

			return new Word(word, firstCapital, notFirstCapital);
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
