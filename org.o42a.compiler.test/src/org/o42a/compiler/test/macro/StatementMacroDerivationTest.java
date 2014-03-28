/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class StatementMacroDerivationTest extends CompilerTestCase {

	@Test
	public void overrideValueMacro() {
		compile(
				"A := integer (",
				"  #T := 1",
				"  = #T",
				")",
				"B := a (",
				"  T = 2",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void overrideConditionMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  #T",
				")",
				"B := a (",
				"  T = false",
				")");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void propagateValueMacro() {
		compile(
				"A := integer (",
				"  #T := 1",
				"  = #T",
				")",
				"B := a");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void propagateConditionMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  #T",
				")",
				"B := a");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(definiteValue(field("b"), ValueType.VOID), voidValue());
	}

}
