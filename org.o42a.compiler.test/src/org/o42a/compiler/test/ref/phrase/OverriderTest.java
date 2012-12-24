/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class OverriderTest extends CompilerTestCase {

	@Test
	public void argument() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  <[arg]> foo = *",
				")",
				"B := a [2]");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void string() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  <'arg'> foo = *",
				")",
				"B := a 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}

	@Test
	public void stringInBrackets() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  <'arg'> foo = *",
				")",
				"B := a ['b']");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}

	@Test
	public void doubleQuotedStringArgument() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  <[arg]> foo = *",
				")",
				"B := a \"b\"");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}

}
