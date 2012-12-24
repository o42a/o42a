/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.ExponentSymbol.LETTER_E;
import static org.o42a.ast.atom.RadixPoint.COMMA_RADIX_POINT;
import static org.o42a.ast.atom.SignOfNumber.POSITIVE_NUMBER;
import static org.o42a.parser.Grammar.number;

import org.junit.Test;
import org.o42a.ast.atom.ExponentNode;
import org.o42a.ast.atom.FractionalPartNode;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class FloatTest extends GrammarTestCase {

	@Test
	public void fractionalPart() {

		final NumberNode number = parse("123,456 789");
		final FractionalPartNode fractional = number.getFractional();

		assertThat(number, hasInteger("123"));
		assertThat(number, unsignedNumber());
		assertThat(fractional, notNullValue());
		assertThat(fractional.getPoint().getType(), is(COMMA_RADIX_POINT));
		assertThat(fractional.getDigits().getDigits(), is("456789"));
		assertThat(number.getExponent(), nullValue());
	}

	@Test
	public void spaceAfterRadixPoint() {

		final NumberNode number = parse("123. 456 789");

		assertThat(number, unsignedInteger("123"));
	}

	@Test
	public void signedExponent() {

		final NumberNode number = parse("123e+456");
		final ExponentNode exponent = number.getExponent();

		assertThat(number, hasInteger("123"));
		assertThat(number, unsignedNumber());
		assertThat(number.getFractional(), nullValue());
		assertThat(exponent.getSymbol().getType(), is(LETTER_E));
		assertThat(exponent.getSign().getType(), is(POSITIVE_NUMBER));
		assertThat(exponent.getDigits().getDigits(), is("456"));
	}

	@Test
	public void spaceAfterExponentSymbol() {

		final NumberNode number = parse("123e 456");

		assertThat(number, unsignedInteger("123"));
	}

	@Test
	public void spaceBeforeExponentSign() {

		final NumberNode number = parse("123e -456");

		assertThat(number, unsignedInteger("123"));
	}

	@Test
	public void spaceAfterExponentSign() {

		final NumberNode number = parse("123e+ 456");

		assertThat(number, unsignedInteger("123"));
	}

	@Test
	public void floatNumber() {

		final NumberNode number = parse("+123.456e789");

		assertThat(number, positiveNumber());
		assertThat(number, hasInteger("123"));
		assertThat(number.getFractional().getDigits().getDigits(), is("456"));
		assertThat(number.getExponent().getDigits().getDigits(), is("789"));
	}

	private NumberNode parse(String text) {
		return parse(number(), text);
	}

}
