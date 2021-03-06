/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class UnaryDefinitionTest extends CompilerTestCase {

	@Test
	public void plus() {
		compile("A := 1. B := +a.");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void minus() {
		compile("A := 1. B := -a.");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(-1L));
	}

	@Test
	public void valueOf() {
		compile("A := 1. B := \\a");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void eagerValue() {
		compile("A := 1. B := a>>");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

}
