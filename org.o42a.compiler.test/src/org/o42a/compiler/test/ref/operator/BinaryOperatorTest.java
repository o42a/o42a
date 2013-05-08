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
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"left\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* + *> Right = ()",
				"    )",
				"  )",
				")",
				"B := a + 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("left"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void subtract() {
		compile(
				"Compute :=> void (",
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"left\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* - *> Right = ()",
				"    )",
				"  )",
				")",
				"B := a - 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("left"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void multiply() {
		compile(
				"Compute :=> void (",
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"left\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* * *> Right = ()",
				"    )",
				"  )",
				")",
				"B := a * 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("left"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void divide() {
		compile(
				"Compute :=> void (",
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"left\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* / *> Right = ()",
				"    )",
				"  )",
				")",
				"B := a / 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("left"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void compare() {
		compile(
				"Compute :=> void (",
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"left\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* <=> *> Right = ()",
				"    )",
				"  )",
				")",
				"B := a <=> 3");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("left"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

	@Test
	public void suffix() {
		compile(
				"Compute :=> void (",
				"  Left :=< string` link",
				"  Right :=< integer` link",
				")",
				"A := string (",
				"  = \"prefix\"",
				"  <*Eval> Compute (",
				"    <*Left | right> Left = $prefix",
				"    <:Right> (",
				"      <* ~ *> Right = ()",
				"    )",
				"  )",
				")",
				"B := 3~a");

		final Obj leftTarget = linkTarget(field("b", "left"));
		final Obj rightTarget = linkTarget(field("b", "right"));

		assertThat(definiteValue(leftTarget, ValueType.STRING), is("prefix"));
		assertThat(definiteValue(rightTarget, ValueType.INTEGER), is(3L));
	}

}
