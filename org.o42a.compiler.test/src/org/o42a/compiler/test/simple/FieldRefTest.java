/*
    Compiler Tests
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.compiler.test.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;


public class FieldRefTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void staticFieldRefInheritance() {
		compile(
				"A := void(Foo := 1).",
				"B := &a:foo");

		final Obj foo = getField(this.a, "foo").getArtifact().toObject();

		assertTrue(this.b.inherits(foo));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void dynamicFieldRefInheritance() {
		compile(
				"A := void(Foo := 1).",
				"B := a:foo");

		final Obj foo = getField(this.a, "foo").getArtifact().toObject();

		assertTrue(this.b.inherits(foo));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void fieldRefDerivation() {
		compile(
				"A := void(Foo := 1).",
				"B := * & a:foo");

		final Artifact<?> foo = getField(this.a, "foo").getArtifact();

		assertFalse(this.b.inherits(foo));
		assertTrue(this.b.inherits(this.context.getIntrinsics().getInteger()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void enclosedFieldRef() {
		compile(
				"A := void(Foo := 1).",
				"B := void(Bar := a:foo)");

		final Obj foo = getField(this.a, "foo").getArtifact().toObject();
		final Obj bar = getField(this.b, "bar").getArtifact().toObject();

		assertTrue(bar.inherits(foo));
		assertEquals(definiteValue(bar), 1L);
	}

	@Test
	public void overrideFieldRef() {
		compile(
				"A := void(Foo := 1).",
				"B := void(Bar := a:foo).",
				"C := b(Bar = 2)");

		final Obj c = getField("c").getArtifact().toObject();

		final Obj foo = getField(this.a, "foo").getArtifact().toObject();
		final Obj bar = getField(c, "bar").getArtifact().toObject();

		assertTrue(bar.derivedFrom(foo));
		assertEquals(definiteValue(bar), 2L);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = getField("a").getArtifact().toObject();
		this.b = getField("b").getArtifact().toObject();
	}

}
