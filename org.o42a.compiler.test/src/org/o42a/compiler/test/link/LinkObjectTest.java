/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class LinkObjectTest extends CompilerTestCase {

	@Test
	public void linkToField() {
		compile(
				"A := 1",
				"B := link = a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj bTarget = linkTarget(b);

		assertThat(
				LINK.interfaceRef(b.type().getParameters()).getType(),
				is(b.getContext().getIntrinsics().getVoid()));
		assertTrue(bTarget.type().getValueType().is(ValueType.INTEGER));
		assertThat(bTarget.getWrapped(), sameInstance(a));
	}

	@Test
	public void typedLink() {
		compile(
				"A := 1",
				"B := integer` link = a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj bTarget = linkTarget(b);

		assertThat(
				LINK.interfaceRef(b.type().getParameters()).getType(),
				is(b.getContext().getIntrinsics().getInteger()));
		assertEquals(ValueType.INTEGER, bTarget.type().getValueType());
		assertThat(definiteValue(bTarget, ValueType.INTEGER), is(1L));
		assertThat(bTarget.getWrapped(), sameInstance(a));
	}

	@Test
	public void linkToRef() {
		compile(
				"A := void (",
				"  Foo := 1.",
				"  Bar := foo` link = foo",
				")",
				"B := a (Foo = 2)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj aFoo = field(a, "foo").toObject();
		final Field aBar = field(a, "bar");
		final Obj aBarTarget = linkTarget(aBar);

		assertThat(aBarTarget.getWrapped(), sameInstance(aFoo));

		final Obj bFoo = field(b, "foo").toObject();
		final Field bBar = field(b, "bar");
		final Obj bBarTarget = linkTarget(bBar);

		assertThat(bBarTarget.getWrapped(), sameInstance(bFoo));
	}

	@Test
	public void staticLink() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  Bar := &foo` link = &foo",
				")",
				"B := a (Foo = 2)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		final Obj aFoo = field(a, "foo").toObject();
		final Field aBar = field(a, "bar");
		final Obj aBarTarget = linkTarget(aBar);

		assertThat(aBarTarget.getWrapped(), sameInstance(aFoo));

		final Field bBar = field(b, "bar");
		final Obj bBarTarget = linkTarget(bBar);

		assertThat(bBarTarget.getWrapped(), sameInstance(aFoo));
	}

}
