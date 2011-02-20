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
package org.o42a.compiler.test.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.util.Source;


public class UnaryInheritanceTest extends CompilerTestCase {

	private Field<?> a;
	private Field<?> b;
	private Field<?> c;

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

		assertThat(definiteValue(this.aBar, Long.class), is(1L));
		assertThat(definiteValue(this.bBar, Long.class), is(2L));
		assertThat(definiteValue(this.cBar, Long.class), is(2L));
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

		assertThat(definiteValue(this.aBar, Long.class), is(-1L));
		assertThat(definiteValue(this.bBar, Long.class), is(-2L));
		assertThat(definiteValue(this.cBar, Long.class), is(-2L));
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.a = field("a");
		this.b = field("b");
		this.c = field("c");

		this.aBar = field(this.a, "bar");
		this.bBar = field(this.b, "bar");
		this.cBar = field(this.c, "bar");
	}

}
