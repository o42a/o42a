/*
    Parser
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.parser;


final class ExpectedStringParser implements Parser<String> {

	private final String expectedString;

	ExpectedStringParser(String expectedString) {
		this.expectedString = expectedString;
	}

	@Override
	public String parse(ParserContext context) {
		for (int i = 0, len = this.expectedString.length(); i < len ; ++i) {
			if (context.next() != this.expectedString.charAt(i)) {
				return null;
			}
		}
		context.acceptAll();
		return this.expectedString;
	}

	@Override
	public String toString() {
		return "StringParser[" + this.expectedString + ']';
	}

}
