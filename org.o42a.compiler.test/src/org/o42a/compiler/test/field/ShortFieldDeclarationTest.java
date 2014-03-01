/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ShortFieldDeclarationTest extends CompilerTestCase {

	@Test
	public void override() {
		compile(
				"A ::= void(",
				"  F := 1",
				")",
				"B ::= a (",
				"  *F (= 2)",
				")");

		assertThat(
				definiteValue(field("a", "f"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("b", "f"), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void overrideWithoutDefinition() {
		compile(
				"A ::= void(",
				"  F := 1",
				")",
				"B ::= a (",
				"  *F",
				")");

		assertThat(
				definiteValue(field("a", "f"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("b", "f"), ValueType.INTEGER),
				is(1L));
	}
}
