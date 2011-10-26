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
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class SubstitutionClauseTest extends CompilerTestCase {

	@Test
	public void topLevelSubstitution() {
		compile(
				"A := void(",
				"  <*[arg]>",
				")",
				"B := a[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void selfAssignment() {
		compile(
				"A := integer(",
				"  <*[arg]> ()",
				")",
				"B := a[2]");

		final Field<?> b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overrider() {
		compile(
				"Val := 1",
				"A :=> integer(",
				"  Foo :=< `integer",
				"  <*[arg]> Foo = ()",
				")",
				"B := a[val]");

		final Field<?> bFoo = field("b", "foo");

		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(1L));
		assertSame(
				field("val").getArtifact().materialize(),
				bFoo.getArtifact().materialize().getWrapped());
	}

}
