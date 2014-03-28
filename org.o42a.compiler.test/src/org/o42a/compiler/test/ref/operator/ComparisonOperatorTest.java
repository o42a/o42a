/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;


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

		assertThat(definiteValue(field("b"), ValueType.VOID), is(Void.VOID));
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

		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void less() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a < 2");

		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void lessOrEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a <= 2");

		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void greater() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a > 2");

		assertThat(definiteValue(field("b"), ValueType.VOID), is(Void.VOID));
	}

	@Test
	public void greaterOrEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a >= 2");

		assertThat(definiteValue(field("b"), ValueType.VOID), is(Void.VOID));
	}

	@Test
	public void compareEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a == 2");

		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void compareNotEquals() {
		compile(
				"A := void (",
				"  <* <=> *> Integer ()",
				")",
				"B := a <> 2");

		assertThat(definiteValue(field("b"), ValueType.VOID), is(Void.VOID));
	}

}
