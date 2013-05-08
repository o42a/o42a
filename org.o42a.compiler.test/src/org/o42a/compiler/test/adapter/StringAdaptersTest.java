/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class StringAdaptersTest extends CompilerTestCase {

	@Test
	public void intToString() {
		compile(
				"A := 123 456 789",
				"B := string (= A)",
				"C := string` link = a",
				"D := string (= 123)");

		assertThat(
				definiteValue(field("a"), ValueType.INTEGER),
				is(123456789L));
		assertThat(
				definiteValue(field("b"), ValueType.STRING),
				is("123456789"));
		assertThat(
				definiteValue(linkTarget(field("c")), ValueType.STRING),
				is("123456789"));
		assertThat(
				definiteValue(field("d"), ValueType.STRING),
				is("123"));
	}

	@Test
	public void floatToString() {
		compile(
				"A := 123 456,789",
				"B := string (= A)",
				"C := string` link = a",
				"D := string (= 123.456)");

		assertThat(
				definiteValue(field("a"), ValueType.FLOAT),
				is(123456.789));
		assertThat(
				definiteValue(field("b"), ValueType.STRING),
				is("123456.789"));
		assertThat(
				definiteValue(linkTarget(field("c")), ValueType.STRING),
				is("123456.789"));
		assertThat(
				definiteValue(field("d"), ValueType.STRING),
				is("123.456"));
	}

}
