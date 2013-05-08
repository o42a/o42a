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
				"A := integer` link = 42",
				"B := a (= 43)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(a), ValueType.INTEGER), is(42L));
		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(43L));

		assertTrue(b.type().inherits(a.type()));
	}

	@Test
	public void inheritLinkTarget() {
		compile(
				"A := integer` link = 42",
				"B := a-> (= 43)");

		final Obj b = field("b").toObject();
		final Obj aTarget = linkTarget(field("a").toObject());

		assertThat(definiteValue(aTarget, ValueType.INTEGER), is(42L));
		assertThat(definiteValue(b, ValueType.INTEGER), is(43L));

		assertTrue(b.type().inherits(aTarget.type()));
		assertTrue(b.getWrapped().type().inherits(aTarget.type()));
	}

	@Test
	public void linkPropagation() {
		compile(
				"A := void (",
				"  Foo := integer` link = 1",
				"  Bar := integer` link = foo",
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
				"  Foo :=< &integer` link",
				"  Bar := &integer` link = foo",
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
				"  F := integer` link = 3",
				")",
				"Lnk :=> integer` link (",
				"  G := 12",
				")",
				"B := a (",
				"  F = lnk ()",
				")");

		assertThat(
				definiteValue(field("b", "f", "g"), ValueType.INTEGER),
				is(12L));
	}

	@Test
	public void parameterizedLinkAncestorUpgrade() {
		compile(
				"A := void (",
				"  F := integer` link = 3",
				")",
				"Lnk :=> link (",
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
				"  F :=<> integer` link = 3",
				")",
				"Lnk :=> integer` link (",
				"  G := 12",
				")",
				"B :=> a (",
				"  F =<> lnk ()",
				")");

		assertThat(
				definiteValue(field("b", "f", "g"), ValueType.INTEGER),
				is(12L));
	}

}
