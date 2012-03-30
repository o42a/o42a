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
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public class FieldInheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Test
	public void inheritField() {
		compile(
				"A := void(Foo := 123).",
				"B := a.",
				"C := b.");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertTrue(bFoo.isPropagated());
		assertEquals(123L, definiteValue(bFoo));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertTrue(cFoo.isPropagated());
		assertEquals(123L, definiteValue(cFoo));
	}

	@Test
	public void overrideField() {
		compile(
				"A := void(Foo := 123).",
				"B := a(Foo = 12).",
				"C := b.");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertFalse(bFoo.isPropagated());
		assertEquals(12L, definiteValue(bFoo));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertTrue(cFoo.isPropagated());
		assertEquals(12L, definiteValue(cFoo));
	}

	@Test
	public void scopedOverride() {
		compile(
				"A := void(Foo := 123).",
				"B := a(Foo @a = 12).",
				"C := b.");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertFalse(bFoo.isPropagated());
		assertEquals(12L, definiteValue(bFoo));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertTrue(cFoo.isPropagated());
		assertEquals(12L, definiteValue(cFoo));
	}

	@Test
	public void overlapField() {
		compile(
				"A := void(Foo := 123).",
				"B := a(Foo := 12).",
				"C := b.");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertEquals(12L, definiteValue(bFoo));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getKey())));

		assertEquals(12L, definiteValue(cFoo));
		assertThat(
				cFoo.getKey(),
				not(equalTo(this.c.member(aFoo.getKey()).getKey())));
	}

	@Test
	public void hiddenOverride() {
		compile(
				"A := void(Foo := 123).",
				"Z := a(Foo := 321).",
				"B := z(Foo @a = 12).",
				"C := b.");

		final Field aFoo = field(this.a, "foo");
		final Field z = field("z");
		final Field zFoo = field(z, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertEquals(321L, definiteValue(zFoo));

		assertEquals(321L, definiteValue(bFoo));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getKey())));
		assertThat(bFoo.getKey(), is(zFoo.getKey()));
		assertTrue(bFoo.isPropagated());

		assertEquals(321L, definiteValue(cFoo));
		assertThat(
				cFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getKey())));
		assertThat(cFoo.getKey(), is(zFoo.getKey()));
		assertTrue(cFoo.isPropagated());

		final Field baFoo =
				this.b.member(aFoo.getKey()).toField().field(USE_CASE);

		assertEquals(12L, definiteValue(baFoo));
		assertFalse(baFoo.isPropagated());

		final Field caFoo =
				this.c.member(aFoo.getKey()).toField().field(USE_CASE);

		assertEquals(12L, definiteValue(caFoo));
		assertTrue(caFoo.isPropagated());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
		this.c = field("c").toObject();
	}

}
