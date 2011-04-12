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
package org.o42a.compiler.test.def;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeDefinitionTest extends CompilerTestCase {

	@Test
	public void conditionalValue() {
		compile(
				"A := integer(",
				"  Condition := `void",
				"  {Condition? = 1. = 0}",
				")",
				"B := a(Condition = false)",
				"C := b");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("b"), Long.class), is(0L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

	@Test
	public void sign() {
		compile(
				"Sign :=> integer(",
				"  Arg :=< integer.",
				"  {",
				"    Arg > 0? = 1",
				"    Arg < 0? = -1",
				"    = 0",
				"  }",
				")",
				"A := sign(Arg = 10)",
				"A1 := A(Arg = -9)",
				"A2 := A1",
				"B := sign(Arg = -10)",
				"C := sign(Arg = 0)");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("a1"), Long.class), is(-1L));
		assertThat(definiteValue(field("a2"), Long.class), is(-1L));
		assertThat(definiteValue(field("b"), Long.class), is(-1L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

	@Test
	public void defaultValue() {
		compile(
				"A := integer(",
				"  Condition := `void",
				"  {Condition? = 1}",
				"  = 0",
				")",
				"B := a(Condition = false)",
				"C := b");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("b"), Long.class), is(0L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

}
