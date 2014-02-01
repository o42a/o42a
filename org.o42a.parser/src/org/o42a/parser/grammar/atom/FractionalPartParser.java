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

import static org.o42a.ast.atom.RadixPoint.COMMA_RADIX_POINT;
import static org.o42a.ast.atom.RadixPoint.DOT_RADIX_POINT;
import static org.o42a.parser.grammar.atom.DigitsParser.DECIMAL_DIGITS;
import static org.o42a.util.string.Characters.isDigit;

import org.o42a.ast.atom.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class FractionalPartParser implements Parser<FractionalPartNode> {

	public static final FractionalPartParser FRACTIONAL_PART =
			new FractionalPartParser();

	private FractionalPartParser() {
	}

	@Override
	public FractionalPartNode parse(ParserContext context) {

		final RadixPoint radixPoint;

		switch (context.next()) {
		case '.':
			radixPoint = DOT_RADIX_POINT;
			break;
		case ',':
			radixPoint = COMMA_RADIX_POINT;
			break;
		default:
			return null;
		}

		final SourcePosition start = context.current().fix();

		if (!isDigit(context.next())) {
			// No space allowed between radix point and next digit,
			// but digits parser generally allows it.
			return null;
		}

		final SignNode<RadixPoint> point = new SignNode<>(
				start,
				context.current().fix(),
				radixPoint);
		final DigitsNode digits = context.parse(DECIMAL_DIGITS);

		if (digits == null) {
			return null;
		}

		return new FractionalPartNode(point, digits);
	}

}
