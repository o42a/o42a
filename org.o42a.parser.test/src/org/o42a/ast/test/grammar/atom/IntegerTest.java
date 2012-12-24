/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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


public class IntegerTest extends GrammarTestCase {

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
