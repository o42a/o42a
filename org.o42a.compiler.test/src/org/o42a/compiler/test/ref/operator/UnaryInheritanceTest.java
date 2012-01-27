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


public class UnaryInheritanceTest extends CompilerTestCase {

	private Field<?> aBar;
	private Field<?> bBar;
	private Field<?> cBar;

	@Test
	public void plus() {
		compile(
				"A := void(",
				"  Foo := 1.",
				"  Bar := +foo.",
				").",
				"B := a(Foo = 2).",
				"C := b.");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(2L));
	}

	@Test
	public void minus() {
		compile(
				"A := void(",
				"  Foo := 1.",
				"  Bar := -foo.",
				").",
				"B := a(Foo = 2).",
				"C := b.");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(-2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(-2L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aBar = field("a", "bar");
		this.bBar = field("b", "bar");
		this.cBar = field("c", "bar");
	}

}
