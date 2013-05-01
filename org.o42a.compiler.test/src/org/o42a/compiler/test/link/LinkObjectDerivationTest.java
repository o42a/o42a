/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class LinkObjectDerivationTest extends CompilerTestCase {

	@Test
	public void inheritLink() {
		compile(
				"A := link (`integer) = 42",
				"B := a (= 43)");

		final Field a = field("a");
		final Field b = field("b");

		final Obj aTarget = linkTarget(a);

		assertThat(definiteValue(aTarget, ValueType.INTEGER), is(42L));
		assertThat(definiteValue(b, ValueType.INTEGER), is(43L));

		assertTrue(b.toObject().type().inherits(aTarget.type()));
		assertTrue(b.toObject().getWrapped().type().inherits(aTarget.type()));
	}

	@Test
	public void linkPropagation() {
		compile(
				"A := void (",
				"  Foo := link (`integer) = 1",
				"  Bar := link (`integer) = foo",
				")",
				"B := a (Foo = 2)");

		final Field aBar = field(field("a"), "bar");
		final Field bBar = field(field("b"), "bar");

		final Obj aBarTarget = linkTarget(aBar);
		final Obj bBarTarget = linkTarget(bBar);

		assertThat(definiteValue(aBarTarget, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bBarTarget, ValueType.INTEGER), is(2L));
	}

	@Test
	public void staticLinkPropagation() {
		compile(
				"A :=> void (",
				"  Foo :=< link (`&integer)",
				"  Bar := link (`&integer) = foo",
				")",
				"B := a (Foo = 2)",
				"C := b",
				"D := b ()");

		final Field bBar = field(field("b"), "bar");
		final Field cBar = field(field("c"), "bar");
		final Field dBar = field(field("d"), "bar");

		final Obj bBarTarget = linkTarget(bBar);
		final Obj cBarTarget = linkTarget(cBar);
		final Obj dBarTarget = linkTarget(dBar);

		assertThat(definiteValue(bBarTarget, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cBarTarget, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(dBarTarget, ValueType.INTEGER), is(2L));
	}

	@Test
	public void linkAncestorUpgrade() {
		compile(
				"A := void (",
				"  F := link (`integer) = 3",
				")",
				"Lnk :=> link (`integer) (",
				"  G := 12",
				")",
				"B := a (",
				"  F = lnk` ()",
				")");

		assertThat(
				definiteValue(field("b", "f", "g"), ValueType.INTEGER),
				is(12L));
	}

	@Test
	public void parameterizedLinkAncestorUpgrade() {
		compile(
				"A := void (",
				"  F := link (`integer) = 3",
				")",
				"Lnk :=> link` (",
				"  G := 12",
				")",
				"B := a (",
				"  F = lnk (`integer)",
				")");

		assertThat(
				definiteValue(field("b", "f", "g"), ValueType.INTEGER),
				is(12L));
	}

	@Test
	public void linkPrototypeAncestorUpgrade() {
		compile(
				"A :=> void (",
				"  F :=<> link (`integer) = 3",
				")",
				"Lnk :=> link (`integer) (",
				"  G := 12",
				")",
				"B :=> a (",
				"  F =<> lnk` ()",
				")");

		assertThat(
				definiteValue(field("b", "f", "g"), ValueType.INTEGER),
				is(12L));
	}

}
