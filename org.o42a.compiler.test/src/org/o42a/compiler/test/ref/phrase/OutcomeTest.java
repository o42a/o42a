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
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class OutcomeTest extends CompilerTestCase {

	@Test
	public void outcomeOfCreatedObject() {
		compile(
				"A :=> void(",
				"  F :=< integer",
				"  <*[] = f> F = ()",
				")",
				"B := a[42]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(42L));
	}

	@Test
	public void outcomeOfTopLevelExpression() {
		compile(
				"A :=> void(",
				"  F :=< integer",
				"  <*> A(",
				"    <*[] = f> F = ()",
				"  )",
				")",
				"B := a[42]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(42L));
	}

	@Test
	public void complexOutcome() {
		compile(
				"A :=> void(",
				"  F :=< string(",
				"    G := 1",
				"  )",
				"  <*'' = f: g> F = *(",
				"    <G> G = *(",
				"       <*[]> = ()",
				"    )",
				"  )",
				")",
				"B := a'value 1'",
				"C := a'value 2' g[2]");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(2L));
	}

}
