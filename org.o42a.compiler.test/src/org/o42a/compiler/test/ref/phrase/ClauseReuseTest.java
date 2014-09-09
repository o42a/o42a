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


public class ClauseReuseTest extends CompilerTestCase {

	@Test
	public void reuseObject() {
		compile(
				"A := string (",
				"  Foo := 1",
				"  <[Foo value] | $object> foo = *",
				"  <Bar> (<'Value'> ())",
				")",
				"B := a[2] bar 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reuseNamedGroup() {
		compile(
				"A := string (",
				"  Foo := 1",
				"  <[Foo value] | bar> foo = *",
				"  <Bar> (<'Value'> ())",
				")",
				"B := a[2] bar 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reuseImplicitGroup() {
		compile(
				"A := string (",
				"  Foo := 1",
				"  <[Foo value] | group> Foo = *",
				"  <*Group> (",
				"    <Bar> (<'Value'> ())",
				"  )",
				")",
				"B := a [2] bar 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reuseInternalGroup() {
		compile(
				"A := string (",
				"  Foo := 1",
				"  <[Foo value] | internal group> Foo = *",
				"  <:Internal group> (",
				"    <Bar> (<'Value'> ())",
				"  )",
				")",
				"B := a [2] bar 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reusePrecedence() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  Bar := \"b\"",
				"  <:Foo group> (<''> *Foo)",
				"  <:Bar group> (<''> *Bar)",
				" <Set | foo group | bar group> ()",
				")",
				"B := a () set 'c'");

		final Field b = field("b");
		final Field foo = field(b, "foo");
		final Field bar = field(b, "bar");

		assertThat(definiteValue(foo, ValueType.STRING), is("a"));
		assertThat(definiteValue(bar, ValueType.STRING), is("c"));
	}

	@Test
	public void reuseParent() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  Bar := \"b\"",
				"  <*Group> (",
				"    <'' | group> *Foo",
				"    <[] | group> *Bar",
				"  )",
				")",
				"B := a 'c' [\"d\"]");

		final Field b = field("b");
		final Field foo = field(b, "foo");
		final Field bar = field(b, "bar");

		assertThat(definiteValue(foo, ValueType.STRING), is("c"));
		assertThat(definiteValue(bar, ValueType.STRING), is("d"));
	}

	@Test
	public void reuseAliased() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  Bar := \"b\"",
				"  <[Foo] | bar> *Foo",
				"  <'Bar'> *Bar",
				")",
				"B := a [\"c\"] 'd'");

		assertThat(definiteValue(field("b", "foo"), ValueType.STRING), is("c"));
		assertThat(definiteValue(field("b", "bar"), ValueType.STRING), is("d"));
	}

	@Test
	public void reuseAliasedInsideGroup() {
		compile(
				"A := void (",
				"  Foo := \"a\"",
				"  Bar := \"b\"",
				"  <*Group> (",
				"    <[Foo] | bar> *Foo",
				"  )",
				"  <'Bar'> *Bar",
				")",
				"B := a [\"c\", 'd']");

		assertThat(definiteValue(field("b", "foo"), ValueType.STRING), is("c"));
		assertThat(definiteValue(field("b", "bar"), ValueType.STRING), is("d"));
	}

}
