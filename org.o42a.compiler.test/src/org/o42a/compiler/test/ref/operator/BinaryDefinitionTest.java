/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class BinaryDefinitionTest extends CompilerTestCase {

	private Field c;
	private Field d;
	private Field e;

	@Test
	public void add() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := a + b.",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(5L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(5L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(5L));
	}

	@Test
	public void subtract() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := a - b.",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(-1L));
	}

	@Test
	public void multiply() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := a * b.",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(6L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(6L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(6L));
	}

	@Test
	public void divide() {
		compile(
				"A := 14.",
				"B := 3.",
				"C := a / b.",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(4L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(4L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(4L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.c = field("c");
		this.d = field("d");
		this.e = field("e");
	}

}
