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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.o42a.core.artifact.object.Derivation.MEMBER_OVERRIDE;

import org.junit.Before;
import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class InheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Before
	public void setUp() {
		compile(
				"A := integer(= 1. Foo := 123456).",
				"B := a(= 2. Foo = 1234567).",
				"C := b().");
		this.a = field("a").getArtifact().toObject();
		this.b = field("b").getArtifact().toObject();
		this.c = field("c").getArtifact().toObject();
	}

	@Test
	public void inheritance() {
		assertThat(
				this.b.type()
				.getAncestor().typeObject(USE_CASE)
				.getScope().toField().getKey().getName(),
				is("a"));
		assertSame(
				this.a,
				this.b.type().getAncestor().typeObject(USE_CASE));
		assertTrue(this.b.type().inherits(
				this.a.type()));
		assertSame(
				this.b,
				this.c.type().getAncestor().typeObject(USE_CASE));
		assertTrue(this.c.type().inherits(this.a.type()));
		assertTrue(this.c.type().inherits(this.b.type()));
	}

	@Test
	public void value() {
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.c, ValueType.INTEGER), is(2L));
	}

	@Test
	public void fieldDeclaration() {

		final Field<?> aFoo = field(this.a, "foo");

		assertThat(aFoo, notNullValue());
		assertFalse(aFoo.isPropagated());
		assertThat(
				aFoo.getArtifact().toObject().type()
				.getAncestor().typeObject(USE_CASE),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void fieldOverride() {

		final Field<?> aFoo = field(this.a, "foo");
		final Field<?> bFoo = field(this.b, "foo");

		assertFalse(bFoo.isPropagated());
		assertTrue(
				bFoo.getArtifact().toObject().type()
				.derivedFrom(
						aFoo.getArtifact().toObject().type(),
						MEMBER_OVERRIDE));
	}

	@Test
	public void fieldPropagation() {

		final Field<?> aFoo = field(this.a, "foo");
		final Field<?> bFoo = field(this.b, "foo");
		final Field<?> cFoo = field(this.c, "foo");

		assertTrue(cFoo.isPropagated());
		assertTrue(
				cFoo.getArtifact().toObject().type()
				.derivedFrom(
						aFoo.getArtifact().toObject().type(),
						MEMBER_OVERRIDE));
		assertTrue(
				cFoo.getArtifact().toObject().type()
				.derivedFrom(
						bFoo.getArtifact().toObject().type(),
						MEMBER_OVERRIDE));
	}

}
