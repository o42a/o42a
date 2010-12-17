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
package org.o42a.compiler.test.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;


public class LinkTest extends CompilerTestCase {

	@Test
	public void linkToField() {
		compile(
				"A := 1;",
				"b := `a.");

		final Obj a = getField("a").getArtifact().toObject();
		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ArtifactKind.LINK, getField("b").getArtifact().getKind());
		assertEquals(1L, definiteValue(b));
		assertSame(a, b.getWrapped());
	}

	@Test
	public void typedLink() {
		compile(
				"A := 1;" +
				"b := (`integer) a.");

		final Obj a = getField("a").getArtifact().toObject();
		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ArtifactKind.LINK, getField("b").getArtifact().getKind());
		assertEquals(1L, definiteValue(b));
		assertSame(a, b.toArtifact().materialize().getWrapped());
	}

	@Test
	public void linkToRef() {
		compile(
				"A := void(Foo := 1, bar := `foo),",
				"b := a(Foo = 2).");

		final Obj a = getField("a").getArtifact().toObject();
		final Obj b = getField("b").getArtifact().toObject();

		final Obj aFoo = getField(a, "foo").getArtifact().toObject();
		final Field<?> aBar = getField(a, "bar");

		assertEquals(ArtifactKind.LINK, aBar.getArtifact().getKind());
		assertSame(aFoo, aBar.getArtifact().materialize().getWrapped());

		final Obj bFoo = getField(b, "foo").getArtifact().toObject();
		final Field<?> bBar = getField(b, "bar");

		assertEquals(ArtifactKind.LINK, bBar.getArtifact().getKind());
		assertSame(bFoo, bBar.getArtifact().materialize().getWrapped());
	}

	@Test
	public void staticLink() {
		compile(
				"A := void(Foo := 1, bar := `&foo),",
				"b := a(Foo = 2).");

		final Obj a = getField("a").getArtifact().toObject();
		final Obj b = getField("b").getArtifact().toObject();

		final Obj aFoo = getField(a, "foo").getArtifact().toObject();
		final Field<?> aBar = getField(a, "bar");

		assertEquals(ArtifactKind.LINK, aBar.getArtifact().getKind());
		assertSame(aFoo, aBar.getArtifact().materialize().getWrapped());

		final Field<?> bBar = getField(b, "bar");

		assertEquals(ArtifactKind.LINK, bBar.getArtifact().getKind());
		assertSame(aFoo, bBar.getArtifact().materialize().getWrapped());
	}

}
