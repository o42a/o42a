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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class BinaryOperatorTest extends CompilerTestCase {

	@Test
	public void add() {
		compile(
				"Compute :=> void(",
				"  Left :=< `void",
				"  Right :=< `integer",
				")",
				"A := void(",
				"  <*Eval> Compute(",
				"    <@Operators: add | eval> Left = void",
				"    <@Operators: operand> Right = integer",
				"  )",
				")",
				"B := a + 3");

		assertTrueVoid(field("b", "left"));
		assertThat(definiteValue(field("b", "right"), Long.class), is(3L));
	}

	@Test
	public void subtract() {
		compile(
				"Compute :=> void(",
				"  Left :=< `void",
				"  Right :=< `integer",
				")",
				"A := void(",
				"  <*Eval> Compute(",
				"    <@Operators: subtract | eval> Left = void",
				"    <@Operators: operand> Right = integer",
				"  )",
				")",
				"B := a - 3");

		assertTrueVoid(field("b", "left"));
		assertThat(definiteValue(field("b", "right"), Long.class), is(3L));
	}

	@Test
	public void multiply() {
		compile(
				"Compute :=> void(",
				"  Left :=< `void",
				"  Right :=< `integer",
				")",
				"A := void(",
				"  <*Eval> Compute(",
				"    <@Operators: multiply | eval> Left = void",
				"    <@Operators: operand> Right = integer",
				"  )",
				")",
				"B := a * 3");

		assertTrueVoid(field("b", "left"));
		assertThat(definiteValue(field("b", "right"), Long.class), is(3L));
	}

	@Test
	public void divide() {
		compile(
				"Compute :=> void(",
				"  Left :=< `void",
				"  Right :=< `integer",
				")",
				"A := void(",
				"  <*Eval> Compute(",
				"    <@Operators: divide | eval> Left = void",
				"    <@Operators: operand> Right = integer",
				"  )",
				")",
				"B := a / 3");

		assertTrueVoid(field("b", "left"));
		assertThat(definiteValue(field("b", "right"), Long.class), is(3L));
	}

}
