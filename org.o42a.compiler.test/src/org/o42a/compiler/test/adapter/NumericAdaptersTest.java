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


public class NumericAdaptersTest extends CompilerTestCase {

	@Test
	public void intToFloat() {
		compile(
				"A := float (= 456)",
				"B := 7890",
				"C := float (= B)",
				"D := float` link = b");

		assertThat(definiteValue(field("a"), ValueType.FLOAT), is(456.0));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(7890L));
		assertThat(definiteValue(field("c"), ValueType.FLOAT), is(7890.0));
		assertThat(
				definiteValue(linkTarget(field("d")), ValueType.FLOAT),
				is(7890.0));
	}

	@Test
	public void operators() {
		compile(
				"A := 0.12 + 42",
				"B := 0.1 * 42",
				"C := float '9' / 2",
				"D := 12.12 - 2");

		assertThat(definiteValue(field("a"), ValueType.FLOAT), is(42.12));
		assertThat(definiteValue(field("b"), ValueType.FLOAT), is(4.2));
		assertThat(definiteValue(field("c"), ValueType.FLOAT), is(4.5));
		assertThat(definiteValue(field("d"), ValueType.FLOAT), is(10.12));
	}

	@Test
	public void comparison() {
		compile(
				"A := 0.12 < 42",
				"B := 0.1 >= 42",
				"C := float '9' == 9",
				"D := 12.12 <> 12");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertTrueVoid(field("c"));
		assertTrueVoid(field("d"));
	}

}
