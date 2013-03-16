/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ComparisonMacroTest extends CompilerTestCase {

	@Test
	public void eq() {
		compile(
				"A := 2 ##eq [2]",
				"B := 3 ##eq [2]");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
	}

	@Test
	public void ne() {
		compile(
				"A := 2 ##ne [3]",
				"B := 2 ##ne [2]");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
	}

	@Test
	public void gt() {
		compile(
				"A := 3 ##gt [2]",
				"B := 3 ##gt [3]",
				"C := 2 ##gt [3]");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertFalseVoid(field("c"));
	}

	@Test
	public void ge() {
		compile(
				"A := 3 ##ge [2]",
				"B := 3 ##ge [3]",
				"C := 2 ##ge [3]");

		assertTrueVoid(field("a"));
		assertTrueVoid(field("b"));
		assertFalseVoid(field("c"));
	}

	@Test
	public void lt() {
		compile(
				"A := 2 ##lt [3]",
				"B := 3 ##lt [3]",
				"C := 3 ##lt [2]");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertFalseVoid(field("c"));
	}

	@Test
	public void le() {
		compile(
				"A := 2 ##le [3]",
				"B := 3 ##le [3]",
				"C := 3 ##le [2]");

		assertTrueVoid(field("a"));
		assertTrueVoid(field("b"));
		assertFalseVoid(field("c"));
	}

}
