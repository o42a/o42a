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
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;


public class LinkInheritanceTest extends CompilerTestCase {

	@Test
	public void linkPropagation() {
		compile(
				"A := void(",
				"  Foo := `1.",
				"  Bar := `foo.",
				").",
				"B := a(Foo = 2).");

		final Field<?> aBar = field(field("a"), "bar");
		final Field<?> bBar = field(field("b"), "bar");

		assertThat(definiteValue(aBar, Long.class), is(1L));
		assertThat(definiteValue(bBar, Long.class), is(2L));
	}

	@Test
	public void staticLinkPropagation() {
		compile(
				"A :=> void(",
				"  Foo :=< `&integer.",
				"  Bar := `foo.",
				").",
				"B := a(Foo = 2).",
				"C := b.",
				"D := b().");

		final Field<?> bBar = field(field("b"), "bar");
		final Field<?> cBar = field(field("c"), "bar");
		final Field<?> dBar = field(field("d"), "bar");

		assertThat(definiteValue(bBar, Long.class), is(2L));
		assertThat(definiteValue(cBar, Long.class), is(2L));
		assertThat(definiteValue(dBar, Long.class), is(2L));
	}

}
