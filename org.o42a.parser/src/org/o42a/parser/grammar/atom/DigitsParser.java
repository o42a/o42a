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

import static org.o42a.ast.atom.Radix.DECIMAL_RADIX;

import org.o42a.ast.atom.DigitsNode;
import org.o42a.ast.atom.Radix;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;


final class DigitsParser implements Parser<DigitsNode> {

	private static final DigitsParser[] parsers;

	static {

		final Radix[] radixes = Radix.values();

		parsers = new DigitsParser[radixes.length];
		for (int i = 0; i < radixes.length; ++i) {
			parsers[i] = new DigitsParser(radixes[i]);
		}
	}

	static final DigitsParser DECIMAL_DIGITS =
			digitsParser(DECIMAL_RADIX);

	static DigitsParser digitsParser(Radix radix) {
		return parsers[radix.ordinal()];
	}

	private final Radix radix;

	private DigitsParser(Radix radix) {
		this.radix = radix;
	}

	@Override
	public DigitsNode parse(ParserContext context) {

		SourcePosition spaceStart = null;
		boolean wrongSpace = false;
		SourcePosition start = null;
		StringBuilder digits = null;

		for (;;) {

			final int c = context.next();

			if (this.radix.isDigit(c)) {
				if (wrongSpace) {
					context.getLogger().warning(
							"invalid_space_in_number",
							new SourceRange(
									spaceStart,
									context.current().fix()),
							"Only a single space character allowed in number");
					wrongSpace = false;
				}
				spaceStart = null;
				if (digits == null) {
					start = context.current().fix();
					digits = new StringBuilder();
				}
				digits.append((char) c);
				continue;
			}
			if (Character.getType(c) == Character.SPACE_SEPARATOR) {
				if (spaceStart == null) {
					spaceStart = context.current().fix();
					if (digits == null) {
						wrongSpace = true;
					}
				} else {
					wrongSpace = true;
				}
				continue;
			}
			if (digits == null) {
				return null;
			}
			context.acceptButLast();

			return new DigitsNode(
					start,
					context.firstUnaccepted().fix(),
					digits.toString());
		}
	}

}
