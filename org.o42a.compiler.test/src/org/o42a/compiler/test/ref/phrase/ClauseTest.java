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


public class ClauseTest extends CompilerTestCase {

	@Test
	public void argument() {
		compile(
				"A := void (<[Arg]> integer)",
				"B := a [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void initializer() {
		compile(
				"A := void (<[= Arg]> integer)",
				"B := a = 2");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void explicitInitializer() {
		compile(
				"A := void (<[= Arg]> integer)",
				"B := a [= 2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void suffix() {
		compile(
				"A := void (<Suffix ~ *> integer)",
				"B := 2 ~ a");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void name() {
		compile(
				"A :=> void (",
				"  Foo :=< integer ",
				"  <Name> Foo = *",
				")",
				"B := a _name (= 2)");

		final Field bFoo = field("b", "foo");

		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void string() {
		compile(
				"A := void (<'Arg'>)",
				"B := a 'b'");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
	}

	@Test
	public void stringInBrackets() {
		compile(
				"A := void (<'Arg'>)",
				"B := a ['b']");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
	}

	@Test
	public void doubleQuotedStringArgument() {
		compile(
				"A := void (<[Arg]>)",
				"B := a \"b\"");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
	}

	@Test
	public void implicit() {
		compile(
				"A := string (<*Implicit> = string(<''> ()))",
				"B := a 'b'");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
	}

	@Test
	public void abstractOverrider() {
		compile(
				"A :=> void (",
				"  Foo :=< \"a\"",
				"  <*Implicit> A (",
				"    <[Arg]> foo = string",
				"  )",
				")",
				"B := a \"b\"");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}
}
