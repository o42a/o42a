/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class BinaryOperatorTest extends CompilerTestCase {

	@Test
	public void add() {
		compile(
				"Compute :=> void (",
				"  Left :=< link (`void)",
				"  Right :=< link (`integer)",
				")",
				"A := void (",
				"  <*Eval> Compute (",
				"    <* + * | eval> Left = $prefix$",
				"    <[]> Right = ()",
				"  )",
				")",
				"B := a + 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertTrueVoid(leftTarget);
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void subtract() {
		compile(
				"Compute :=> void (",
				"  Left :=< link (`void)",
				"  Right :=< link (`integer)",
				")",
				"A := void (",
				"  <*Eval> Compute (",
				"    <* - * | eval> Left = $prefix$",
				"    <[]> Right = ()",
				"  )",
				")",
				"B := a - 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertTrueVoid(leftTarget);
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void multiply() {
		compile(
				"Compute :=> void (",
				"  Left :=< link (`void)",
				"  Right :=< link (`integer)",
				")",
				"A := void (",
				"  <*Eval> Compute (",
				"    <* * * | eval> Left = $prefix$",
				"    <[]> Right = ()",
				"  )",
				")",
				"B := a * 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertTrueVoid(leftTarget);
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void divide() {
		compile(
				"Compute :=> void (",
				"  Left :=< link (`void)",
				"  Right :=< link (`integer)",
				")",
				"A := void (",
				"  <*Eval> Compute (",
				"    <* / * | eval> Left = $prefix$",
				"    <[]> Right = ()",
				"  )",
				")",
				"B := a / 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertTrueVoid(leftTarget);
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void compare() {
		compile(
				"Compute :=> void (",
				"  Left :=< link (`void)",
				"  Right :=< link (`integer)",
				")",
				"A := void (",
				"  <*Eval> Compute (",
				"    <* <=> * | eval> Left = $prefix$",
				"    <[]> Right = ()",
				"  )",
				")",
				"B := a <=> 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertTrueVoid(leftTarget);
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

}
