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

import static org.o42a.ast.atom.Radix.DECIMAL_RADIX;
import static org.o42a.parser.grammar.atom.DigitsParser.digitsParser;
import static org.o42a.parser.grammar.atom.RadixParser.RADIX;
import static org.o42a.parser.grammar.atom.SignOfNumberParser.SIGN_OF_NUMBER;

import org.o42a.ast.atom.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class NumberParser implements Parser<NumberNode> {

	public static final NumberParser NUMBER = new NumberParser();

	private NumberParser() {
	}

	@Override
	public NumberNode parse(ParserContext context) {

		final SignNode<SignOfNumber> sign = context.push(SIGN_OF_NUMBER);
		final SignNode<Radix> radixPrefix = context.parse(RADIX);
		final Radix radix =
				radixPrefix == null ? DECIMAL_RADIX : radixPrefix.getType();
		final DigitsNode integer = context.parse(digitsParser(radix));

		if (integer == null) {
			if (radixPrefix == null) {
				return null;
			}
			context.getLogger().error(
					"missing_digits",
					radixPrefix,
					"The number has no digits");
		}

		return new NumberNode(sign, radixPrefix, integer);
	}

}
