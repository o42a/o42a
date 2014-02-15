/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.core.member.MemberId.SCOPE_FIELD_ID;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Derivation;
import org.o42a.core.value.ValueType;


public class PropagationTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void overrideAncestor() {
		compile(
				"A := void (Foo := 1. Bar := foo ())",
				"B := &a (Foo = 3)");

		final Obj aFoo = field(this.a, "foo").toObject();
		final Obj bFoo = field(this.b, "foo").toObject();

		assertTrue(bFoo.type().derivedFrom(
				aFoo.type(),
				Derivation.MEMBER_OVERRIDE));

		final Obj aBar = field(this.a, "bar").toObject();
		final Obj bBar = field(this.b, "bar").toObject();

		assertThat(definiteValue(bBar, ValueType.INTEGER), is(3L));
		assertTrue(bBar.type().derivedFrom(
				aBar.type(),
				Derivation.MEMBER_OVERRIDE));
		assertTrue(bBar.type().derivedFrom(
				aFoo.type(),
				Derivation.INHERITANCE));
		assertFalse(bBar.type().derivedFrom(
				aFoo.type(),
				Derivation.PROPAGATION));
		assertTrue(
				bBar.type()
				.getAncestor().getType().type().derivedFrom(
						aFoo.type(),
						Derivation.MEMBER_OVERRIDE));
		assertTrue(bBar.type().inherits(
				bFoo.type()));

		final Field aFooScope =
				aFoo.member(SCOPE_FIELD_ID, Accessor.INHERITANT)
				.toField()
				.field(USE_CASE);
		final Field bFooScope =
				bFoo.member(SCOPE_FIELD_ID, Accessor.INHERITANT)
				.toField()
				.field(USE_CASE);

		assertThat(aFooScope.toObject(), is((Object) this.a));
		assertThat(bFooScope.toObject(), is((Object) this.b));
	}

	@Test
	public void propagateField() {
		compile(
				"A := void (Foo := void (Bar := 1))",
				"B := &a (Foo = * (Bar = 2))");

		final Obj aFoo = field(this.a, "foo").toObject();
		final Obj bFoo = field(this.b, "foo").toObject();

		assertTrue(bFoo.type().derivedFrom(
				aFoo.type(),
				Derivation.MEMBER_OVERRIDE));

		final Obj aBar = field(aFoo, "bar").toObject();
		final Obj bBar = field(bFoo, "bar").toObject();

		assertTrue(bBar.type().derivedFrom(
				aBar.type(),
				Derivation.MEMBER_OVERRIDE));
	}

	@Test
	public void upgradeAncestor() {
		compile(
				"Foo := 1",
				"Bar := foo (= 2)",
				"A := void (Foo := upgrade ancestor: foo (= 3))",
				"B := a (Foo = bar ())");

		final Obj foo = field("foo").toObject();
		final Obj bar = field("bar").toObject();
		final Obj aFoo = field(this.a, "foo").toObject();
		final Obj bFoo = field(this.b, "foo").toObject();

		assertTrue(aFoo.type().inherits(foo.type()));
		assertFalse(aFoo.type().inherits(bar.type()));
		assertTrue(bFoo.type().inherits(bar.type()));
		assertTrue(bFoo.type().derivedFrom(aFoo.type()));

		assertThat(definiteValue(foo, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bar, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(aFoo, ValueType.INTEGER), is(3L));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(3L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
	}


}
