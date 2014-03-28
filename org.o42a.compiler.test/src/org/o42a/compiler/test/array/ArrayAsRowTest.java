/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.ValueKnowledge.RUNTIME_VALUE;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.array.Array;


public class ArrayAsRowTest extends CompilerTestCase {

	@Test
	public void arrayAsRow() {
		compile(
				"A := integer` array [[1, 2, 3]]",
				"B := a: as row");

		assertThat(valueOf(field("b")).getKnowledge(), is(RUNTIME_VALUE));
	}

	@Test
	public void emptyArrayAsRow() {
		compile(
				"A := integer` array [[]]",
				"B := a: as row");

		final Array array = definiteValue(field("b"));

		assertThat(array.length(), is(0));
	}

	@Test
	public void falseArrayAsRow() {
		compile(
				"A := integer` array (False, = [1, 2, 3])",
				"B := a: as row");

		assertThat(valueOf(field("b")), falseValue());
	}

}
