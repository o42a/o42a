/*
    Compiler Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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


public class GroupTest extends CompilerTestCase {

	@Test
	public void group() {
		compile(
				"A := integer(",
				"  <*[arg]> ()",
				")",
				"B := A[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overriderInGroup() {
		compile(
				"A := void(",
				"  Foo := 1.",
				"  <Set> foo = *(",
				"    <*[value]> ()",
				"  )",
				")",
				"B := A() set[2]");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overriderInImplicitGroup() {
		compile(
				"A := void(",
				"  Foo := 1.",
				"  <*implied> foo = *(",
				"    <*[value]> ()",
				"  )",
				")",
				"B := A[2]");

		final Field<?> b = field("b");
		final Field<?> foo = field(b, "foo");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupInGroup() {
		compile(
				"A := integer(",
				"  <Set> (",
				"    <*[value]> ()",
				"  )",
				")",
				"B := A() set[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupIn2ImplicitGroups() {
		compile(
				"A := integer(",
				"  <*> (",
				"    <*> (",
				"      <*[value]> ()",
				"    )",
				"  )",
				")",
				"B := A[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void assignerIn2ImplicitGroups() {
		compile(
				"A := integer(",
				"  <*foo> (",
				"    <*bar> (",
				"      <*[value]> = integer()",
				"    )",
				"  )",
				")",
				"B := A[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void groupInImplicitGroup() {
		compile(
				"A := integer(",
				"  <*implicit> (",
				"    <*[value]> ()",
				"  )",
				")",
				"B := A[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

}
