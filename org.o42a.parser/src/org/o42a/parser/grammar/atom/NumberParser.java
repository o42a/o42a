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

import static org.o42a.parser.grammar.atom.DigitsParser.DECIMAL_DIGITS;
import static org.o42a.parser.grammar.atom.DigitsParser.digitsParser;
import static org.o42a.parser.grammar.atom.ExponentParser.EXPONENT;
import static org.o42a.parser.grammar.atom.FractionalPartParser.FRACTIONAL_PART;
import static org.o42a.parser.grammar.atom.RadixParser.RADIX;
import static org.o42a.parser.grammar.atom.SignOfNumberParser.SIGN_OF_NUMBER;

import org.o42a.ast.atom.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.parser.ParserLogger;
import org.o42a.util.log.LogInfo;


public class NumberParser implements Parser<NumberNode> {

	public static final NumberParser NUMBER = new NumberParser();

	static void missingDigits(ParserLogger logger, LogInfo location) {
		logger.error(
				"missing_digits",
				location,
				"The number has no digits");
	}

	private NumberParser() {
	}

	@Override
	public NumberNode parse(ParserContext context) {

		final SignNode<SignOfNumber> sign = context.push(SIGN_OF_NUMBER);

		if (sign != null) {
			context.skipComments(false, sign);
		}

		final SignNode<Radix> radix = context.parse(RADIX);
		final DigitsNode integer = context.parse(
				radix != null
				? digitsParser(radix.getType())
				: DECIMAL_DIGITS);

		if (integer == null) {
			if (radix == null) {
				return null;
			}
			missingDigits(context.getLogger(), radix);
		}
		if (radix != null) {
			return new NumberNode(sign, radix, integer, null, null);
		}

		final FractionalPartNode fractional = context.parse(FRACTIONAL_PART);
		final ExponentNode exponent = context.parse(EXPONENT);

		return context.acceptComments(
				false,
				new NumberNode(sign, radix, integer, fractional, exponent));
	}

}
