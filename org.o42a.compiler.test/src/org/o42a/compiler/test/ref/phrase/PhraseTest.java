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


public class PhraseTest extends CompilerTestCase {

	@Test
	public void overriderInsideOverrider() {
		compile(
				"A := void(",
				"  Foo := integer(Bar :=> string. = 1)",
				"  <*[]> foo = *(<*''> bar = *)",
				")",
				"B := a[2]'b'");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");
		final Field<?> bar = field(foo, "bar");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(bar, ValueType.STRING), is("b"));
	}

	@Test
	public void overriderInsideClause() {
		compile(
				"A := integer(",
				"  Foo :=> string",
				"  <*[]> a(<*''> foo = string)",
				")",
				"B := a[2]'b'");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}

	@Test
	public void nestedFieldRef() {
		compile(
				"A :=> void(",
				"  Val :=< `integer",
				"  Sum :=> integer(",
				"    Inc :=< `integer",
				"    = Val + inc",
				"  )",
				"  <*> Sum(",
				"    <*[]> Inc = ()",
				"  )",
				")",
				"B := a(Val = 1)",
				"C := b[10]");

		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(11L));
	}

}
