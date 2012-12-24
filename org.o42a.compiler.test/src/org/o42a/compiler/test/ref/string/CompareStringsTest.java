/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.string;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class CompareStringsTest extends CompilerTestCase {

	@Test
	public void equal() {
		compile(
				"Res1 := \"abc\" == \"abc\"",
				"Res2 := \"abc\" == \"a\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
	}

	@Test
	public void notEqual() {
		compile(
				"Res1 := \"abc\" <> \"a\"",
				"Res2 := \"abc\" <> \"abc\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
	}

	@Test
	public void less() {
		compile(
				"Res1 := \"a\" < \"b\"",
				"Res2 := \"a\" < \"a\"",
				"Res3 := \"b\" < \"a\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
		assertFalseVoid(field("res3"));
	}

	@Test
	public void lessOrEqual() {
		compile(
				"Res1 := \"a\" <= \"b\"",
				"Res2 := \"a\" <= \"a\"",
				"Res3 := \"b\" <= \"a\"");

		assertTrueVoid(field("res1"));
		assertTrueVoid(field("res2"));
		assertFalseVoid(field("res3"));
	}

	@Test
	public void greater() {
		compile(
				"Res1 := \"a\" > \"b\"",
				"Res2 := \"a\" > \"a\"",
				"Res3 := \"b\" > \"a\"");

		assertFalseVoid(field("res1"));
		assertFalseVoid(field("res2"));
		assertTrueVoid(field("res3"));
	}

	@Test
	public void greaterOrEqual() {
		compile(
				"Res1 := \"a\" >= \"b\"",
				"Res2 := \"a\" >= \"a\"",
				"Res3 := \"b\" >= \"a\"");

		assertFalseVoid(field("res1"));
		assertTrueVoid(field("res2"));
		assertTrueVoid(field("res3"));
	}

}
