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


public class ConditionalDefinitionTest extends CompilerTestCase {

	@Test
	public void conditionalValue() {
		compile(
				"A := void(",
				"  Condition := `void",
				"  Value := integer(Condition->? = 1. = 0)",
				")",
				"B := a (Condition = false)",
				"C := b");

		assertThat(
				definiteValue(field("a", "value"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("b", "value"), ValueType.INTEGER),
				is(0L));
		assertThat(
				definiteValue(field("c", "value"), ValueType.INTEGER),
				is(0L));
	}

	@Test
	public void sign() {
		compile(
				"Sign :=> integer (",
				"  Arg :=< integer",
				"  Arg > 0? = 1",
				"  Arg < 0? = -1",
				"  = 0",
				").",
				"A := sign (Arg = 10)",
				"A1 := A (Arg = -9)",
				"A2 := A1",
				"B := sign (Arg = -10)",
				"C := sign (Arg = 0)");

		assertThat(
				definiteValue(field("a"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("a1"), ValueType.INTEGER),
				is(-1L));
		assertThat(
				definiteValue(field("a2"), ValueType.INTEGER),
				is(-1L));
		assertThat(
				definiteValue(field("b"), ValueType.INTEGER),
				is(-1L));
		assertThat(
				definiteValue(field("c"), ValueType.INTEGER),
				is(0L));
	}

	@Test
	public void conditionalImperativeValue() {
		compile(
				"A := void (",
				"  Condition := `void",
				"  Value := integer({Condition->? = 1. = 0})",
				")",
				"B := a (Condition = false)",
				"C := b");

		assertThat(
				definiteValue(field("a", "value"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("b", "value"), ValueType.INTEGER),
				is(0L));
		assertThat(
				definiteValue(field("c", "value"), ValueType.INTEGER),
				is(0L));
	}

}
