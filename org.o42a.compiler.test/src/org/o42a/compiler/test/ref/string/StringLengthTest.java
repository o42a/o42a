/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class StringLengthTest extends CompilerTestCase {

	@Test
	public void nonEmptyString() {
		compile("Len := \"a\\42a\\b\\n\": length");

		assertThat(definiteValue(field("len"), ValueType.INTEGER), is(4L));
	}

	@Test
	public void emptyString() {
		compile("Len := \"\": length");

		assertThat(definiteValue(field("len"), ValueType.INTEGER), is(0L));
	}

	@Test
	public void falseString() {
		compile("Len := string(False): length");

		assertThat(valueOf(field("len"), ValueType.INTEGER), falseValue());
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Len := rt-string 'abc': length");

		assertThat(valueOf(field("len"), ValueType.INTEGER), runtimeValue());
	}

}
