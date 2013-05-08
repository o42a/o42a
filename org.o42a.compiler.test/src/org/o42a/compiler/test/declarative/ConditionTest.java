/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ConditionTest extends CompilerTestCase {

	@Test
	public void condition() {
		compile(
				"A := void (",
				"  Condition := void",
				"  Value := void (Condition)",
				")",
				"B := a (Condition = false)",
				"C := b");

		assertTrueVoid(field("a", "value"));
		assertFalseVoid(field("b", "value"));
		assertFalseVoid(field("c", "value"));
	}

	@Test
	public void not() {
		compile(
				"A := void (",
				"  Condition := void` link = false",
				"  Value := void (--Condition->)",
				")",
				"B := a (Condition = void)",
				"C := b");

		assertTrueVoid(field("a", "value"));
		assertFalseVoid(field("b", "value"));
		assertFalseVoid(field("c", "value"));
	}

	@Test
	public void issue() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = Void. = False",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertFalseVoid(field("c"));
	}

	@Test
	public void unlessIssue() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = False. = Void",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertFalseVoid(field("a"));
		assertTrueVoid(field("b"));
		assertTrueVoid(field("c"));
	}

}
