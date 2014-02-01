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

import static org.o42a.ast.atom.Radix.BINARY_RADIX;
import static org.o42a.ast.atom.Radix.HEXADECIMAL_RADIX;

import org.o42a.ast.atom.Radix;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class RadixParser implements Parser<SignNode<Radix>> {

	static final RadixParser RADIX = new RadixParser();

	private RadixParser() {
	}

	@Override
	public SignNode<Radix> parse(ParserContext context) {
		if (context.next() != '0') {
			return null;
		}

		final SourcePosition start = context.current().fix();
		final Radix radix;

		switch (context.next()) {
		case 'x':
		case 'X':
			radix = HEXADECIMAL_RADIX;
			break;
		case 'b':
		case 'B':
			radix = BINARY_RADIX;
			break;
		default:
			return null;
		}

		context.acceptAll();

		return new SignNode<>(start, context.current().fix(), radix);
	}

}
