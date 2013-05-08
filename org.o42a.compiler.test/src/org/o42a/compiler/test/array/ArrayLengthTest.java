/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ArrayLengthTest extends CompilerTestCase {

	@Test
	public void rowLength() {
		compile(
				"Array := indexed` link = [1, 2]",
				"Len := `array: length");

		assertThat(
				definiteValue(linkTarget(field("len")), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void runtimeConstructedRowLength() {
		compile(
				"Use namespace 'Test'",
				"Array := indexed` link = [1, rt-integer '2']",
				"Len := `array: length");

		assertThat(
				definiteValue(linkTarget(field("len")), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void arrayLength() {
		compile(
				"Array := `//integer` //array [[1, 2, 3]]",
				"Len := array: length");

		assertThat(definiteValue(field("len"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void emptyRowLength() {
		compile(
				"Array := string` //row [[]]",
				"Len := array: length");

		assertThat(definiteValue(field("len"), ValueType.INTEGER), is(0L));
	}

	@Test
	public void emptyArrayLength() {
		compile(
				"Array := string` //array [[]]",
				"Len := array: length");

		assertThat(definiteValue(field("len"), ValueType.INTEGER), is(0L));
	}

}
