/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class DefinitionSentenceTest extends CompilerTestCase {

	@Test
	public void conditionAfterField() {
		compile(
				"A := 42",
				"False");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(42L));
		assertThat(valueOf(this.module, ValueType.VOID), falseValue());
	}

	@Test
	public void valueAfterField() {
		compile(
				"A := string (",
				"  Foo := 42",
				"  = \"value\"",
				")");

		assertThat(
				definiteValue(field("a", "foo"), ValueType.INTEGER),
				is(42L));
		assertThat(
				definiteValue(field("a"), ValueType.STRING),
				is("value"));
	}

	@Test
	public void fieldConditionField() {
		compile(
				"A := 42",
				"False",
				"B := 34");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(42L));
		assertThat(valueOf(this.module, ValueType.VOID), falseValue());
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(34L));
	}

}
