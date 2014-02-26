/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
				"B := integer` link = a");

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
				"A ::= void (Foo := 1. Bar := `/a: foo)",
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
				"B := `a",
				"C := b->",
				"D := b->->");

		assertThat(
				definiteValue(linkTarget(field("a")), ValueType.INTEGER),
				is(1L));

		final Obj bTarget = linkTarget(field("b"));

		assertTrue(bTarget.type().getValueType().isLink());
		assertThat(
				LINK.interfaceRef(bTarget.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				definiteValue(linkTarget(bTarget), ValueType.INTEGER),
				is(1L));

		final Obj c = field("c").toObject();

		assertTrue(c.type().getValueType().isLink());
		assertThat(
				LINK.interfaceRef(c.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				definiteValue(linkTarget(c), ValueType.INTEGER),
				is(1L));

		assertThat(definiteValue(field("d"), ValueType.INTEGER), is(1L));
	}

}
