/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ArrayItemTest extends CompilerTestCase {

	@Test
	public void shortRowItem() {
		compile(
				"Array := [1, 2, 3]",
				"Item := array [1]->");

		assertThat(definiteValue(field("item"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void fullRowItem() {
		compile(
				"Array := [1, 2, 3]",
				"Item := array: item` (Index = 1)");

		assertThat(
				definiteValue(
						linkTarget(field("item")),
						ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void arrayItemValue() {
		compile(
				"Array := [1, 2, 3]",
				"Value := integer (= array [1])");

		assertThat(
				definiteValue(field("value"), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void arrayItemExpression() {
		compile(
				"Array := [1, 2, 3]",
				"Expression := array [1] + 2");

		assertThat(
				definiteValue(field("expression"), ValueType.INTEGER),
				is(4L));
	}

}
