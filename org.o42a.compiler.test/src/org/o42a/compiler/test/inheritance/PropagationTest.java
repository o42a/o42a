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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Derivation;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.util.Source;


public class PropagationTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void overrideAncestor() {
		compile(
				"A := void(Foo := 1; bar := foo()).",
				"B := &a(Foo = 3)");

		final Obj aFoo = field(this.a, "foo").getArtifact().toObject();
		final Obj bFoo = field(this.b, "foo").getArtifact().toObject();

		assertTrue(bFoo.type().useBy(USE_CASE).derivedFrom(
				aFoo.type().useBy(USE_CASE), Derivation.MEMBER_OVERRIDE));

		final Obj aBar = field(this.a, "bar").getArtifact().toObject();
		final Obj bBar = field(this.b, "bar").getArtifact().toObject();

		assertThat(definiteValue(bBar, Long.class), is(3L));
		assertTrue(bBar.type().useBy(USE_CASE).derivedFrom(
				aBar.type().useBy(USE_CASE),
				Derivation.MEMBER_OVERRIDE));
		assertTrue(bBar.type().useBy(USE_CASE).derivedFrom(
				aFoo.type().useBy(USE_CASE),
				Derivation.INHERITANCE));
		assertFalse(bBar.type().useBy(USE_CASE).derivedFrom(
				aFoo.type().useBy(USE_CASE),
				Derivation.PROPAGATION));
		assertTrue(
				bBar.type().useBy(USE_CASE)
				.getAncestor().type(USE_CASE).derivedFrom(
						aFoo.type().useBy(USE_CASE),
						Derivation.MEMBER_OVERRIDE));
		assertTrue(bBar.type().useBy(USE_CASE).inherits(
				bFoo.type().useBy(USE_CASE)));

		final Field<?> aFooScope =
			field(aFoo, "_scope", Accessor.INHERITANT);
		final Field<?> bFooScope =
			field(bFoo, "_scope", Accessor.INHERITANT);

		assertThat(
				aFooScope.getArtifact(),
				is((Object) this.a));
		assertThat(
				bFooScope.getArtifact(),
				is((Object) this.b));
	}

	@Test
	public void propagateField() {
		compile(
				"A := void(Foo := void(Bar := 1));",
				"b := &a(Foo = *(Bar = 2))");

		final Obj aFoo = field(this.a, "foo").getArtifact().toObject();
		final Obj bFoo = field(this.b, "foo").getArtifact().toObject();

		assertTrue(bFoo.type().useBy(USE_CASE).derivedFrom(
				aFoo.type().useBy(USE_CASE),
				Derivation.MEMBER_OVERRIDE));

		final Obj aBar = field(aFoo, "bar").getArtifact().toObject();
		final Obj bBar = field(bFoo, "bar").getArtifact().toObject();

		assertTrue(bBar.type().useBy(USE_CASE).derivedFrom(
				aBar.type().useBy(USE_CASE),
				Derivation.MEMBER_OVERRIDE));
	}

	@Test
	public void upgradeAncestor() {
		compile(
				"Foo := 1;",
				"bar := &foo(=2);",
				"a := void(Foo := &$foo(=3));",
				"b := &a(Foo = &bar())");

		final Obj foo = field("foo").getArtifact().toObject();
		final Obj bar = field("bar").getArtifact().toObject();
		final Obj aFoo = field(this.a, "foo").getArtifact().toObject();
		final Obj bFoo = field(this.b, "foo").getArtifact().toObject();

		assertTrue(aFoo.type().useBy(USE_CASE).inherits(
				foo.type().useBy(USE_CASE)));
		assertFalse(aFoo.type().useBy(USE_CASE).inherits(
				bar.type().useBy(USE_CASE)));
		assertTrue(bFoo.type().useBy(USE_CASE).inherits(
				bar.type().useBy(USE_CASE)));
		assertTrue(bFoo.type().useBy(USE_CASE).derivedFrom(
				aFoo.type().useBy(USE_CASE)));

		assertThat(definiteValue(foo, Long.class), is(1L));
		assertThat(definiteValue(bar, Long.class), is(2L));
		assertThat(definiteValue(aFoo, Long.class), is(3L));
		assertThat(definiteValue(bFoo, Long.class), is(2L));
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.a = field("a").getArtifact().toObject();
		this.b = field("b").getArtifact().toObject();
	}


}
