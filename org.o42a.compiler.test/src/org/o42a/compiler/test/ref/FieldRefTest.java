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
package org.o42a.compiler.test.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;


public class FieldRefTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void staticFieldRefInheritance() {
		compile(
				"A := void(Foo := 1).",
				"B := &a: foo.");

		final Obj foo = field(this.a, "foo").getArtifact().toObject();

		assertTrue(this.b.type().inherits(foo.type()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void dynamicFieldRefInheritance() {
		compile(
				"A := void(Foo := 1).",
				"B := a: foo");

		final Obj foo = field(this.a, "foo").getArtifact().toObject();

		assertTrue(this.b.type().inherits(foo.type()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void fieldRefDerivation() {
		compile(
				"A := void(Foo := 1).",
				"B := * & a: foo");

		final Obj foo = field(this.a, "foo").getArtifact().materialize();

		assertFalse(this.b.type().inherits(foo.type()));
		assertTrue(this.b.type().inherits(
				this.context.getIntrinsics().getInteger().type()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void enclosedFieldRef() {
		compile(
				"A := void(Foo := 1).",
				"B := void(Bar := a: foo)");

		final Obj foo = field(this.a, "foo").getArtifact().toObject();
		final Obj bar = field(this.b, "bar").getArtifact().toObject();

		assertTrue(bar.type().inherits(foo.type()));
		assertEquals(definiteValue(bar), 1L);
	}

	@Test
	public void overrideFieldRef() {
		compile(
				"A := void(Foo := 1).",
				"B := void(Bar := a: foo).",
				"C := b(Bar = a: foo(= 2))");

		final Obj c = field("c").getArtifact().toObject();

		final Obj foo = field(this.a, "foo").getArtifact().toObject();
		final Obj bar = field(c, "bar").getArtifact().toObject();

		assertTrue(bar.type().derivedFrom(foo.type()));
		assertEquals(definiteValue(bar), 2L);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").getArtifact().toObject();
		this.b = field("b").getArtifact().toObject();
	}

}
