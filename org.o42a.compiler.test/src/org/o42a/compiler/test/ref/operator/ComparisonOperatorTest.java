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
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ComparisonOperatorTest extends CompilerTestCase {

	@Test
	public void equals() {
		compile(
				"Compare :=> void(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: equals | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a == void");

		assertTrueVoid(field("b"));
	}

	@Test
	public void notEquals() {
		compile(
				"Compare :=> void(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: equals | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a <> void");

		assertFalseVoid(field("b"));
	}

	@Test
	public void less() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a < 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void lessOrEquals() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a <= 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void greater() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a > 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void greaterOrEquals() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a >= 2");

		assertTrueVoid(field("b"));
	}

	@Test
	public void compareEquals() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a == 2");

		assertFalseVoid(field("b"));
	}

	@Test
	public void compareNotEquals() {
		compile(
				"Compare :=> integer(",
				"  Left :=< `void",
				")",
				"A := void(",
				"  <*Cmp> Compare(",
				"    <@Operators: compare | cmp> Left = void",
				"    <@Operators: operand> ()",
				"  )",
				")",
				"B := a <> 2");

		assertTrueVoid(field("b"));
	}

}
