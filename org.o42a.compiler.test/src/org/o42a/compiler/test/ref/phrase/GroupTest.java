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


public class GroupTest extends CompilerTestCase {

	@Test
	public void group() {
		compile(
				"A := integer (",
				"  <[Arg]> ()",
				")",
				"B := A [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overriderInGroup() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  <Set> foo = * (",
				"    <[Value]> ()",
				"  )",
				")",
				"B := A _set [2]");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overriderInImplicitGroup() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  <*Implied> foo = * (",
				"    <[value]> ()",
				"  )",
				")",
				"B := A [2]");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupInGroup() {
		compile(
				"A := integer (",
				"  <Set> (",
				"    <[Value]> ()",
				"  )",
				")",
				"B := A _set [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupIn2ImplicitGroups() {
		compile(
				"A := integer (",
				"  <*> (",
				"    <*> (",
				"      <[Value]> ()",
				"    )",
				"  )",
				")",
				"B := A [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void assignerIn2ImplicitGroups() {
		compile(
				"A := integer (",
				"  <*Foo> (",
				"    <*Bar> (",
				"      <[Value]> = integer ()",
				"    )",
				"  )",
				")",
				"B := A [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupInImplicitGroup() {
		compile(
				"A := integer (",
				"  <*Implicit> (",
				"    <[Value]> ()",
				"  )",
				")",
				"B := A [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

}
