/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ShortAdapterDeclarationTest extends CompilerTestCase {

	@Test
	public void declareNew() {
		compile(
				"Foo ::=> 1",
				"@Foo (= 2)",
				"Bar := ::@@foo");

		assertThat(
				definiteValue(field("foo"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("bar"), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void declareNewWithoutDefinition() {
		compile(
				"Foo ::=> 1",
				"@Foo",
				"Bar := ::@@foo");

		assertThat(
				definiteValue(field("foo"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("bar"), ValueType.INTEGER),
				is(1L));
	}

	@Test
	public void override() {
		compile(
				"Foo ::=> 1",
				"A ::= void (",
				"  @Foo (= 2)",
				"  F := :@@foo",
				")",
				"B ::= a(",
				"  @Foo (= 3)",
				")");

		assertThat(
				definiteValue(field("foo"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("a", "f"), ValueType.INTEGER),
				is(2L));
		assertThat(
				definiteValue(field("b", "f"), ValueType.INTEGER),
				is(3L));
	}

	@Test
	public void overrideWithoutDefinition() {
		compile(
				"Foo ::=> 1",
				"A ::= void (",
				"  @Foo (= 2)",
				"  F := :@@foo",
				")",
				"B ::= a(",
				"  @Foo",
				")");

		assertThat(
				definiteValue(field("foo"), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(field("a", "f"), ValueType.INTEGER),
				is(2L));
		assertThat(
				definiteValue(field("b", "f"), ValueType.INTEGER),
				is(2L));
	}

}
