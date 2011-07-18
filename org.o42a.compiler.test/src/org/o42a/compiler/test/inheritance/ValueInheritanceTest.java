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
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class ValueInheritanceTest extends CompilerTestCase {

	private Field<?> a;
	private Field<?> b;

	@Test
	public void inheritValue() {
		compile("A := 1; b := a");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void propagateValue() {
		compile("A := 1; b := a");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void overrideValue() {
		compile(
				"A := 1",
				"B := a(=2)");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overridePropagatedValue() {
		compile(
				"A := 1;",
				"b := &a(=2)");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a");
		this.b = field("b");
	}

}
