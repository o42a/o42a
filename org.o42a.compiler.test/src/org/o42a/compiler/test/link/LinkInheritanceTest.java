/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class LinkInheritanceTest extends CompilerTestCase {

	@Test
	public void inheritLink() {
		compile(
				"A := `42",
				"B := a (= 43)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(a), ValueType.INTEGER), is(42L));
		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(43L));

		assertThat(b.type().inherits(a.type()), is(true));
	}

	@Test
	public void inheritLinkTarget() {
		compile(
				"A := `42",
				"B := a-> (= 43)");

		final Obj b = field("b").toObject();
		final Obj aTarget = linkTarget(field("a"));

		assertThat(definiteValue(aTarget, ValueType.INTEGER), is(42L));
		assertThat(definiteValue(b, ValueType.INTEGER), is(43L));

		assertThat(b.type().inherits(aTarget.type()), is(true));
		assertThat(b.getWrapped().type().inherits(aTarget.type()), is(true));
	}

	@Test
	public void linkPropagation() {
		compile(
				"A := void (",
				"  Foo := `1",
				"  Bar := foo",
				")",
				"B := a (Foo = 2)");

		final Field aBar = field("a", "bar");
		final Field bBar = field("b", "bar");

		assertThat(
				definiteValue(linkTarget(aBar), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(linkTarget(bBar), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void linkPrototypePropagation() {
		compile(
				"A :=> void (",
				"  Foo :=<> integer` link ()",
				")",
				"B := a (Foo => *)");

		assertThat(
				LINK.interfaceRef(
						field("a", "foo")
						.toObject()
						.type()
						.getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				LINK.interfaceRef(
						field("b", "foo")
						.toObject()
						.type()
						.getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void linkTargetPropagation() {
		compile(
				"A := void (",
				"  Foo := `1",
				"  Bar := foo->",
				")",
				"B := a (Foo = 2)");

		final Field aBar = field("a", "bar");
		final Field bBar = field("b", "bar");

		assertThat(definiteValue(aBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bBar, ValueType.INTEGER), is(2L));
	}

	@Test
	public void staticLinkPropagation() {
		compile(
				"A ::= void (",
				"  Foo := `1",
				"  Bar := /a: foo",
				")",
				"B := a (Foo = 2)",
				"C := b",
				"D := b ()");

		final Field bBar = field("b", "bar");
		final Field cBar = field("c", "bar");
		final Field dBar = field("d", "bar");

		assertThat(
				definiteValue(linkTarget(bBar), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(linkTarget(cBar), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(linkTarget(dBar), ValueType.INTEGER),
				is(1L));
	}

	@Test
	public void staticLinkTargetPropagation() {
		compile(
				"A ::= void (",
				"  Foo := `1",
				"  Bar := /a: foo->",
				")",
				"B := a (Foo = 2)",
				"C := b",
				"D := b ()");

		final Field bBar = field("b", "bar");
		final Field cBar = field("c", "bar");
		final Field dBar = field("d", "bar");

		assertThat(definiteValue(bBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(cBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(dBar, ValueType.INTEGER), is(1L));
	}

}
