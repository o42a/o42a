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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignOfNumber;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.string.Characters;


final class SignOfNumberParser
		implements Parser<SignNode<SignOfNumber>> {

	static final SignOfNumberParser SIGN_OF_NUMBER = new SignOfNumberParser();

	private SignOfNumberParser() {
	}

	@Override
	public SignNode<SignOfNumber> parse(ParserContext context) {

		final SignOfNumber sign;

		switch (context.next()) {
		case '+':
			sign = SignOfNumber.POSITIVE_NUMBER;
			break;
		case Characters.MINUS_SIGN:
		case '-':
			sign = SignOfNumber.NEGATIVE_NUMBER;
			break;
		default:
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		return new SignNode<>(start, context.current().fix(), sign);
	}

}
