/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ComparisonMacroTest extends CompilerTestCase {

	@Test
	public void eq() {
		compile(
				"A := 2 ##eq [2]",
				"B := 3 ##eq [2]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void ne() {
		compile(
				"A := 2 ##ne [3]",
				"B := 2 ##ne [2]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void gt() {
		compile(
				"A := 3 ##gt [2]",
				"B := 3 ##gt [3]",
				"C := 2 ##gt [3]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("c"), ValueType.VOID), falseValue());
	}

	@Test
	public void ge() {
		compile(
				"A := 3 ##ge [2]",
				"B := 3 ##ge [3]",
				"C := 2 ##ge [3]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(definiteValue(field("b"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("c"), ValueType.VOID), falseValue());
	}

	@Test
	public void lt() {
		compile(
				"A := 2 ##lt [3]",
				"B := 3 ##lt [3]",
				"C := 3 ##lt [2]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("c"), ValueType.VOID), falseValue());
	}

	@Test
	public void le() {
		compile(
				"A := 2 ##le [3]",
				"B := 3 ##le [3]",
				"C := 3 ##le [2]");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(definiteValue(field("b"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("c"), ValueType.VOID), falseValue());
	}

}
