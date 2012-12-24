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
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* == * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a == void");

		assertTrueVoid(field("b"));
	}

	@Test
	public void notEquals() {
		compile(
				"Compare :=> void (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* == * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a <> void");

		assertFalseVoid(field("b"));
	}

	@Test
	public void less() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a < 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void lessOrEquals() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a <= 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void greater() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a > 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void greaterOrEquals() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a >= 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void compareEquals() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a == 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void compareNotEquals() {
		compile(
				"Compare :=> integer (",
				"  Left :=< `void",
				")",
				"A := void (",
				"  <*Cmp> Compare (",
				"    <* <=> * | cmp> Left = void",
				"    <[]> ()",
				"  )",
				")",
				"B := a <> 2");

		assertTrueVoid(field("b"));
	}

}
