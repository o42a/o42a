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


public class AdapterRefTest extends CompilerTestCase {

	@Test
	public void adapterRef() {
		compile(
				"A := void (",
				"  @Integer := 1",
				")",
				"B := a @@integer");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void memberOfAdapterRef() {
		compile(
				"A := void (",
				"  F := 1",
				")",
				"B := void (",
				"  @a := * (",
				"    F = 2",
				"  )",
				")",
				"C := b: f @a");

		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void preferInheritedOverAdapted() {
		compile(
				"A := void (",
				"  F := 1",
				")",
				"B := A (",
				"  F = 2",
				"  @a := * (",
				"    F = 3",
				"  )",
				")",
				"C := b: f @a");

		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(2L));
	}

}
