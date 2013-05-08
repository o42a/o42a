/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ComparisonOperatorTest extends CompilerTestCase {

	@Test
	public void equals() {
		compile(
				"Compare :=> void (",
				"  Right :=< integer` link",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* == *> Right = ()",
				"  )",
				")",
				"B := a == 23");

		assertTrueVoid(field("b"));
	}

	@Test
	public void notEquals() {
		compile(
				"Compare :=> void (",
				"  Right :=< integer` link",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* == *> Right = ()",
				"  )",
				")",
				"B := a <> 44");

		assertFalseVoid(field("b"));
	}

	@Test
	public void less() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a < 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void lessOrEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a <= 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void greater() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a > 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void greaterOrEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a >= 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void compareEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a == 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void compareNotEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a <> 2");

		assertTrueVoid(field("b"));
	}

}
