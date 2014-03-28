/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.string;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class CompareStringsTest extends CompilerTestCase {

	@Test
	public void equal() {
		compile(
				"Res1 := \"abc\" == \"abc\"",
				"Res2 := \"abc\" == \"a\"");

		assertThat(definiteValue(field("res1"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("res2"), ValueType.VOID), falseValue());
	}

	@Test
	public void notEqual() {
		compile(
				"Res1 := \"abc\" <> \"a\"",
				"Res2 := \"abc\" <> \"abc\"");

		assertThat(definiteValue(field("res1"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("res2"), ValueType.VOID), falseValue());
	}

	@Test
	public void less() {
		compile(
				"Res1 := \"a\" < \"b\"",
				"Res2 := \"a\" < \"a\"",
				"Res3 := \"b\" < \"a\"");

		assertThat(definiteValue(field("res1"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("res2"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("res3"), ValueType.VOID), falseValue());
	}

	@Test
	public void lessOrEqual() {
		compile(
				"Res1 := \"a\" <= \"b\"",
				"Res2 := \"a\" <= \"a\"",
				"Res3 := \"b\" <= \"a\"");

		assertThat(definiteValue(field("res1"), ValueType.VOID), voidValue());
		assertThat(definiteValue(field("res2"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("res3"), ValueType.VOID), falseValue());
	}

	@Test
	public void greater() {
		compile(
				"Res1 := \"a\" > \"b\"",
				"Res2 := \"a\" > \"a\"",
				"Res3 := \"b\" > \"a\"");

		assertThat(valueOf(field("res1"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("res2"), ValueType.VOID), falseValue());
		assertThat(definiteValue(field("res3"), ValueType.VOID), voidValue());
	}

	@Test
	public void greaterOrEqual() {
		compile(
				"Res1 := \"a\" >= \"b\"",
				"Res2 := \"a\" >= \"a\"",
				"Res3 := \"b\" >= \"a\"");

		assertThat(valueOf(field("res1"), ValueType.VOID), falseValue());
		assertThat(definiteValue(field("res2"), ValueType.VOID), voidValue());
		assertThat(definiteValue(field("res3"), ValueType.VOID), voidValue());
	}

}
