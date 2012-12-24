/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class MacroExpansionTest extends CompilerTestCase {

	@Test
	public void linkInterface() {
		compile(
				"#A := integer",
				"B := (`#a) 123");

		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(123L));
	}

	@Test
	public void linkObject() {
		compile(
				"#A := integer",
				"B := link (`#a) 123");

		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(123L));
	}

	@Test
	public void trueCondition() {
		compile(
				"#A := 5",
				"B := void (#A)");

		assertTrueVoid(field("b"));
	}

	@Test
	public void falseCondition() {
		compile(
				"#A := false",
				"B := void (#A)");

		assertFalseVoid(field("b"));
	}

	@Test
	public void selfAssignment() {
		compile(
				"#A := 5",
				"B := integer (= #A)");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(5L));
	}

	@Test
	public void selfAssignmentWithAdapter() {
		compile(
				"#A := 5",
				"B := float (= #A)");

		assertThat(definiteValue(field("b"), ValueType.FLOAT), is(5.0));
	}

	@Test
	public void linkTarget() {
		compile(
				"#A := 5",
				"B := (`float) #a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.FLOAT),
				is(5.0));
	}

	@Test
	public void overrider() {
		compile(
				"A := void (" +
				"  F := 1",
				")",
				"#T := 2",
				"B := a (",
				"  F = #t",
				")");

		assertThat(definiteValue(field("a", "f"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b", "f"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void rightArithmeticOperand() {
		compile(
				"#T := 1",
				"A := 2 + #t");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void rightComparisonOperand() {
		compile(
				"#T := 1",
				"A := 0 > #t");

		assertFalseVoid(field("a"));
	}

	@Test
	public void rightEqualityOperand() {
		compile(
				"#T := 3",
				"A := 3 == #t");

		assertTrueVoid(field("a"));
	}

	@Test
	public void argument() {
		compile(
				"A :=> integer (",
				"  <[]!> ()",
				")",
				"#T := 123",
				"B := a [#t]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(123L));
	}

	@Test
	public void overriderArgument() {
		compile(
				"A :=> void (",
				"  F :=< string",
				"  <[]!> F = ()",
				")",
				"#T := \"test\"",
				"B := a [#t]");

		assertThat(
				definiteValue(field("b", "f"), ValueType.STRING),
				is("test"));
	}

}
