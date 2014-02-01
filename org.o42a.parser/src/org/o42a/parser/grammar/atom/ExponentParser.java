/*
    Parser
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.ast.atom.ExponentSymbol.LETTER_E;
import static org.o42a.parser.grammar.atom.DigitsParser.DECIMAL_DIGITS;
import static org.o42a.parser.grammar.atom.SignOfNumberParser.SIGN_OF_NUMBER;
import static org.o42a.util.string.Characters.isDigit;

import org.o42a.ast.atom.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class ExponentParser implements Parser<ExponentNode> {

	static final ExponentParser EXPONENT = new ExponentParser();

	private ExponentParser() {
	}

	@Override
	public ExponentNode parse(ParserContext context) {

		final int first = context.next();

		if (first != 'e' && first != 'E') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<ExponentSymbol> symbol = new SignNode<>(
				start,
				context.current().fix(),
				LETTER_E);
		final SignNode<SignOfNumber> sign = context.push(SIGN_OF_NUMBER);

		if (!isDigit(context.pendingOrNext())) {
			return null;
		}

		final DigitsNode digits = context.parse(DECIMAL_DIGITS);

		if (digits == null) {
			return null;
		}

		return new ExponentNode(symbol, sign, digits);
	}

}
