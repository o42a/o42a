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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Derivation;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;


public class InheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Before
	public void setUp() {
		compile(
				"A := void(Foo := 123456); ",
				"b := &a(Foo = 1234567)");
		this.a = getField("a").getArtifact().toObject();
		this.b = getField("b").getArtifact().toObject();
	}

	@Test
	public void inheritance() {
		assertThat(
				this.b.getAncestor().getType().toArtifact()
				.getScope().toField().getKey().getName(),
				is("a"));
		assertSame(this.a, this.b.getAncestor().getType().toArtifact());
		assertTrue(this.b.inherits(this.a));
	}

	@Test
	public void fieldDeclaration() {

		final Field<?> aFoo = getField(this.a, "foo");

		assertThat(aFoo, notNullValue());
		assertFalse(aFoo.isPropagated());
		assertThat(
				aFoo.getArtifact().toObject()
				.getAncestor().getType().toArtifact(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void fieldOverride() {

		final Field<?> found = getField(this.b, "foo");

		assertFalse(found.isPropagated());

		final Field<?> overridden = getField(this.a, "foo");

		assertTrue(
				found.getArtifact().toObject().derivedFrom(
						overridden.getArtifact().toObject(),
						Derivation.MEMBER_OVERRIDE));
	}

}
