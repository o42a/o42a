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
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.value.ValueType;


public class LinkObjectDerivationTest extends CompilerTestCase {

	@Test
	public void inheritLink() {
		compile(
				"A := link` (`integer) [42]",
				"B := a (= 43)");

		final Field<?> a = field("a");
		final Field<?> b = field("b");

		final Obj aTarget = definiteValue(a, ObjectLink.class).getTarget();

		assertThat(definiteValue(aTarget, ValueType.INTEGER), is(42L));
		assertThat(definiteValue(b, ValueType.INTEGER), is(43L));

		assertTrue(b.toObject().type().inherits(aTarget.type()));
		assertTrue(b.toObject().getWrapped().type().inherits(aTarget.type()));
	}

	@Test
	public void linkPropagation() {
		compile(
				"A := void (",
				"  Foo := link` (`integer) [1]",
				"  Bar := link` (`foo) [foo]",
				")",
				"B := a (Foo = 2)");

		final Field<?> aBar = field(field("a"), "bar");
		final Field<?> bBar = field(field("b"), "bar");

		final Obj aBarTarget =
				definiteValue(aBar, ObjectLink.class).getTarget();
		final Obj bBarTarget =
				definiteValue(bBar, ObjectLink.class).getTarget();

		assertThat(definiteValue(aBarTarget, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bBarTarget, ValueType.INTEGER), is(2L));
	}

	@Test
	public void staticLinkPropagation() {
		compile(
				"A :=> void (",
				"  Foo :=< link` (`&integer)",
				"  Bar := link` (`&integer) [foo]",
				")",
				"B := a (Foo = 2)",
				"C := b",
				"D := b ()");

		final Field<?> bBar = field(field("b"), "bar");
		final Field<?> cBar = field(field("c"), "bar");
		final Field<?> dBar = field(field("d"), "bar");

		final Obj bBarTarget =
				definiteValue(bBar, ObjectLink.class).getTarget();
		final Obj cBarTarget =
				definiteValue(cBar, ObjectLink.class).getTarget();
		final Obj dBarTarget =
				definiteValue(dBar, ObjectLink.class).getTarget();

		assertThat(definiteValue(bBarTarget, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cBarTarget, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(dBarTarget, ValueType.INTEGER), is(2L));
	}

}
