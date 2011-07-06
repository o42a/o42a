/*
    Compiler Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
				"A := string(",
				"  Foo := 1.",
				"  <*[foo value] | $object$> foo = *.",
				"  <bar> (<*'value'>).",
				").",
				"B := a[2]bar'b'.");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reuseGroup() {
		compile(
				"A := string(",
				"  Foo := 1.",
				"  <*[foo value] | group> foo = *.",
				"  <*group> (",
				"    <bar> (<*'value'>).",
				"  )",
				").",
				"B := a[2]bar'b'.");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.STRING), is("b"));
		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void reusePrecedence() {
		compile(
				"A := void(",
				"  Foo := \"a\".",
				"  Bar := \"b\".",
				"  <Foo group> (<*''> Foo = *).",
				"  <Bar group> (<*''> Bar = *).",
				" <Set | foo group | bar group> ().",
				").",
				"B := a () set 'c'");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");
		final Field<?> bar = field(b, "bar");

		assertThat(definiteValue(foo, ValueType.STRING), is("a"));
		assertThat(definiteValue(bar, ValueType.STRING), is("c"));
	}

	@Test
	public void reuseParent() {
		compile(
				"A := void(",
				"  Foo := \"a\".",
				"  Bar := \"b\".",
				"  <*Group> (",
				"    <*'' | group> Foo = *.",
				"    <*[] | group> Bar = *.",
				"  ).",
				").",
				"B := a 'c' [\"d\"]");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");
		final Field<?> bar = field(b, "bar");

		assertThat(definiteValue(foo, ValueType.STRING), is("c"));
		assertThat(definiteValue(bar, ValueType.STRING), is("d"));
	}

}
