/*
    Compiler Tests
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;


public class ExpressionPropagationTest extends CompilerTestCase {

	@Test
	public void callPropagation() {
		compile(
				"A := void(",
				"  Value := integer(= 1).",
				"  Foo := integer(",
				"    = Value().",
				"  ).",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field<?> a = field("a");
		final Field<?> b = field("b");
		final Field<?> c = field("c");

		final Field<?> aFoo = field(a, "foo");
		final Field<?> bFoo = field(b, "foo");
		final Field<?> cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, Long.class), is(1L));
		assertThat(definiteValue(bFoo, Long.class), is(2L));
		assertThat(definiteValue(cFoo, Long.class), is(2L));
	}

	@Test
	public void fieldCallPropagation() {
		compile(
				"A := void(",
				"  Container := void(",
				"    Value := integer(= 1).",
				"  ).",
				"  Foo := integer(",
				"    = Container: value().",
				"  ).",
				").",
				"B := A(",
				"  Container = *(",
				"    Value = *(= 2).",
				"  ).",
				").",
				"C := B.");

		final Field<?> a = field("a");
		final Field<?> b = field("b");
		final Field<?> c = field("c");

		final Field<?> aFoo = field(a, "foo");
		final Field<?> bFoo = field(b, "foo");
		final Field<?> cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, Long.class), is(1L));
		assertThat(definiteValue(bFoo, Long.class), is(2L));
		assertThat(definiteValue(cFoo, Long.class), is(2L));
	}

	@Test
	public void scopeDependentFieldInheritance() {
		compile(
				"A := void(",
				"  Value := integer(",
				"    = 1.",
				"    Field := integer(= value).",
				"  ).",
				"  Foo := value: field().",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field<?> a = field("a");
		final Field<?> b = field("b");
		final Field<?> c = field("c");

		final Field<?> aFoo = field(a, "foo");
		final Field<?> bFoo = field(b, "foo");
		final Field<?> cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, Long.class), is(1L));
		assertThat(definiteValue(bFoo, Long.class), is(2L));
		assertThat(definiteValue(cFoo, Long.class), is(2L));
	}

	@Test
	public void scopeDependentFieldCallPropagation() {
		compile(
				"A := void(",
				"  Value := integer(",
				"    = 1.",
				"    Field := integer(= value).",
				"  ).",
				"  Foo := integer(",
				"    = Value: field().",
				"  ).",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field<?> a = field("a");
		final Field<?> b = field("b");
		final Field<?> c = field("c");

		final Field<?> aFoo = field(a, "foo");
		final Field<?> bFoo = field(b, "foo");
		final Field<?> cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, Long.class), is(1L));
		assertThat(definiteValue(bFoo, Long.class), is(2L));
		assertThat(definiteValue(cFoo, Long.class), is(2L));
	}

}
