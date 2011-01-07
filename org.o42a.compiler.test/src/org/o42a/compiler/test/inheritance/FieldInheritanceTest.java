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
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;


public class FieldInheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void inheritField() {
		compile(
				"A := void(Foo := 123).",
				"B := &a");

		final Field<?> aFoo = getField(this.a, "foo");
		final Field<?> bFoo = getField(this.b, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertTrue(bFoo.isPropagated());
		assertEquals(123L, definiteValue(bFoo));
	}

	@Test
	public void overrideField() {
		compile(
				"A := void(Foo := 123).",
				"B := &a(Foo = 12)");

		final Field<?> aFoo = getField(this.a, "foo");
		final Field<?> bFoo = getField(this.b, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertFalse(bFoo.isPropagated());
		assertEquals(12L, definiteValue(bFoo));
	}

	@Test
	public void scopedOverride() {
		compile(
				"A := void(Foo := 123).",
				"B := &a(Foo@a = 12)");

		final Field<?> aFoo = getField(this.a, "foo");
		final Field<?> bFoo = getField(this.b, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertFalse(bFoo.isPropagated());
		assertEquals(12L, definiteValue(bFoo));
	}

	@Test
	public void overlapField() {
		compile(
				"A := void(Foo := 123).",
				"B := &a(Foo := 12)");

		final Field<?> aFoo = getField(this.a, "foo");
		final Field<?> bFoo = getField(this.b, "foo");

		assertEquals(12L, definiteValue(bFoo));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getKey())));
	}

	@Test
	public void hiddenOverride() {
		compile(
				"A := void(Foo := 123).",
				"Z := &a(Foo := 321).",
				"B := &z(Foo@a = 12)");

		final Field<?> aFoo = getField(this.a, "foo");
		final Field<?> z = getField("z");
		final Field<?> zFoo = getField(z, "foo");
		final Field<?> bFoo = getField(this.b, "foo");

		assertEquals(321L, definiteValue(bFoo));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getKey())));
		assertEquals(321L, definiteValue(zFoo));
		assertThat(bFoo.getKey(), is(zFoo.getKey()));
		assertTrue(bFoo.isPropagated());

		final Field<?> baFoo = this.b.member(aFoo.getKey()).toField();

		assertEquals(12L, definiteValue(baFoo));
		assertFalse(baFoo.isPropagated());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = getField("a").getArtifact().toObject();
		this.b = getField("b").getArtifact().toObject();
	}

}
