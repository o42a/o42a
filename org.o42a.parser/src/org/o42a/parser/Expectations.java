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

import java.util.Arrays;

import org.o42a.util.ArrayUtil;


public class Expectations {

	private final ParserContext context;
	private final Parser<?>[] expectations;

	Expectations(ParserContext context) {
		this.context = context;
		this.expectations = new Parser<?>[0];
	}

	Expectations(ParserContext context, Parser<?>[] expectations) {
		this.context = context;
		this.expectations = expectations;
	}

	public Expectations expect(Parser<?> expectation) {
		assert expectation != null :
			"Expectation not specified";
		return new Expectations(
				this.context,
				ArrayUtil.append(this.expectations, expectation));
	}

	public final Expectations setContext(ParserContext context) {
		return new Expectations(context, this.expectations);
	}

	public final Expectations expect(char expectedChar) {
		return expect(new CharParser(expectedChar));
	}

	public final Expectations expect(String expectedString) {
		if (expectedString.length() == 1) {
			return expect(expectedString.charAt(0));
		}
		return expect(new ExpectedStringParser(expectedString));
	}

	public final <T> T parse(Parser<T> parser) {
		return this.context.parse(parser, this);
	}

	public final <T> T push(Parser<T> parser) {
		return this.context.push(parser, this);
	}

	public final <T> T checkFor(Parser<T> parser) {
		return this.context.checkFor(parser, this);
	}

	@Override
	public String toString() {
		return "Expectations" + Arrays.toString(this.expectations);
	}

	boolean asExpected(ParserContext context) {
		for (Parser<?> expectation : this.expectations) {
			if (context.push(expectation) != null) {
				return true;
			}
		}
		return false;
	}

}
