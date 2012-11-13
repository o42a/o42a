/*
    Compiler Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class LinkTest extends CompilerTestCase {

	@Test
	public void linkToField() {
		compile(
				"A := 1",
				"B := `a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();
		final Obj bTarget = linkTarget(b);

		assertThat(definiteValue(bTarget, ValueType.INTEGER), is(1L));
		assertSame(a, bTarget.getWrapped());
	}

	@Test
	public void typedLink() {
		compile(
				"A := 1",
				"B := (`integer) a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();
		final Obj bTarget = linkTarget(b);

		assertThat(definiteValue(bTarget, ValueType.INTEGER), is(1L));
		assertSame(a, bTarget.getWrapped());
	}

	@Test
	public void linkToRef() {
		compile(
				"A := void (Foo := 1. Bar := `foo)",
				"B := a (Foo = 2)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj aFoo = field(a, "foo").toObject();
		final Field aBar = field(a, "bar");
		final Obj aBarTarget = linkTarget(aBar);

		assertSame(aFoo, aBarTarget.getWrapped());

		final Obj bFoo = field(b, "foo").toObject();
		final Field bBar = field(b, "bar");
		final Obj bBarTarget = linkTarget(bBar);

		assertSame(bFoo, bBarTarget.getWrapped());
	}

	@Test
	public void staticLink() {
		compile(
				"A := void (Foo := 1. Bar := `&foo)",
				"B := a (Foo = 2)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj aFoo = field(a, "foo").toObject();
		final Field aBar = field(a, "bar");
		final Obj aBarTarget = linkTarget(aBar);

		assertSame(aFoo, aBarTarget.getWrapped());

		final Field bBar = field(b, "bar");
		final Obj bBarTarget = linkTarget(bBar);

		assertSame(aFoo, bBarTarget.getWrapped());
	}

	@Test
	public void linkToLink() {
		compile(
				"A := `1",
				"B := `a`",
				"C := b",
				"D := b->");

		assertThat(
				definiteValue(linkTarget(field("a")), ValueType.INTEGER),
				is(1L));

		final Obj bTarget = linkTarget(field("b"));

		assertTrue(bTarget.value().getValueType().isLink());
		assertThat(
				LINK.interfaceRef(bTarget.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				definiteValue(linkTarget(bTarget), ValueType.INTEGER),
				is(1L));

		final Obj c = field("c").toObject();

		assertTrue(c.value().getValueType().isLink());
		assertThat(
				LINK.interfaceRef(c.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				definiteValue(linkTarget(c), ValueType.INTEGER),
				is(1L));

		assertThat(definiteValue(field("d"), ValueType.INTEGER), is(1L));
	}

}
