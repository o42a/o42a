/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class OutcomeTest extends CompilerTestCase {

	@Test
	public void outcomeOfCreatedObject() {
		compile(
				"A :=> void(",
				"  F :=< integer",
				"  <[] = f> F = ()",
				")",
				"B := a [42]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(42L));
	}

	@Test
	public void outcomeOfTopLevelExpression() {
		compile(
				"A :=> void(",
				"  F :=< integer",
				"  <*> A (",
				"    <[] = f> F = ()",
				"  )",
				")",
				"B := a [42]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(42L));
	}

	@Test
	public void complexOutcome() {
		compile(
				"A :=> void(",
				"  F :=< string(",
				"    G := 1",
				"  )",
				"  <'' = f: g> F = * (",
				"    <G> G = * (",
				"       <[]> ()",
				"    )",
				"  )",
				")",
				"B := a 'value 1'",
				"C := a 'value 2' g [2]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(2L));
	}

}
