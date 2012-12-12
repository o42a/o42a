/*
    Parser Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.Radix.BINARY_RADIX;
import static org.o42a.ast.atom.Radix.HEXADECIMAL_RADIX;
import static org.o42a.parser.Grammar.number;

import org.junit.Test;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class NumberTest extends GrammarTestCase {

	@Test
	public void unsignedDecimal() {
		assertThat(parse("12345"), is(unsignedInteger("12345")));
	}

	@Test
	public void spaceSeparated() {
		assertThat(parse("1 234 567"), is(unsignedInteger("1234567")));
	}

	@Test
	public void tooManySpaces() {
		expectError("invalid_space_in_number");
		expectError("invalid_space_in_number");

		final NumberNode result = parse("1  234  567    ");

		assertThat(result, is(unsignedInteger("1234567")));
	}

	@Test
	public void nlAfterNumber() {

		final NumberNode result = parse("1 234\n 567    ");

		assertThat(result, is(unsignedInteger("1234")));
		assertThat(this.worker.position().offset(), is(5L));
	}

	@Test
	public void positiveInteger() {
		assertThat(parse("+ ~~comment~~ 12345"), is(positiveInteger("12345")));
	}

	@Test
	public void negativeInteger() {
		assertThat(parse("- ~~comment~~ 12345"), is(negativeInteger("12345")));
		assertThat(
				parse("\u2212 ~~comment~~ 12 345"),
				is(negativeInteger("12345")));
	}

	@Test
	public void hexInteger() {

		final NumberNode number = parse("+ 0x001 fe");

		assertThat(number, is(positiveInteger("001fe")));
		assertThat(number.getRadix(), is(HEXADECIMAL_RADIX));
	}

	@Test
	public void binInteger() {

		final NumberNode number = parse("+ 0b001 10g");

		assertThat(number, is(positiveInteger("00110")));
		assertThat(number.getRadix(), is(BINARY_RADIX));
	}

	@Test
	public void spaceAfterRadix() {
		expectError("invalid_space_in_number");

		final NumberNode number = parse("+ 0x fe");

		assertThat(number, is(positiveInteger("fe")));
		assertThat(number.getRadix(), is(HEXADECIMAL_RADIX));
	}

	@Test
	public void missingDigits() {
		expectError("missing_digits");

		final NumberNode number = parse("0x");

		assertThat(number.getSign(), is(nullValue()));
		assertThat(number.getInteger(), is(nullValue()));
		assertThat(number.getRadix(), is(HEXADECIMAL_RADIX));
	}

	private NumberNode parse(String text) {
		return parse(number(), text);
	}

}
