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


public class StringCharTest extends CompilerTestCase {

	@Test
	public void firstChar() {
		compile("Chr := \"abc\"[0]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("a"));
	}

	@Test
	public void lastChar() {
		compile("Chr := \"abc\"[2]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("c"));
	}

	@Test
	public void someChar() {
		compile("Chr := \"abc\"[1]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("b"));
	}

	@Test
	public void negativeCharIndex() {
		expectError("compiler.invalid_char_index");

		compile("Chr := \"abc\"[-1]");

		assertThat(valueOf(field("chr"), ValueType.STRING), falseValue());
	}

	@Test
	public void invalidCharIndex() {
		expectError("compiler.invalid_char_index");

		compile("Chr := \"abc\"[3]");

		assertThat(valueOf(field("chr"), ValueType.STRING), falseValue());
	}

	@Test
	public void falseString() {
		compile(
				"Str := string(False)",
				"Chr := str[0]");

		assertThat(valueOf(field("chr"), ValueType.STRING), falseValue());
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Chr := rt-string 'abc' [0]");

		assertThat(valueOf(field("chr"), ValueType.STRING), runtimeValue());
	}

	@Test
	public void falseIndex() {
		compile("Chr := \"abc\"[integer(False)]");

		assertThat(valueOf(field("chr"), ValueType.STRING), falseValue());
	}

	@Test
	public void runtimeIndex() {
		compile(
				"Use namespace 'Test'",
				"Chr := \"abc\"[rt-integer '1']");

		assertThat(valueOf(field("chr"), ValueType.STRING), runtimeValue());
	}

}
