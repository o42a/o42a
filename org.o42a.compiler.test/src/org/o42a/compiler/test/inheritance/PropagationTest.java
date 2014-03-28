/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.member.MemberId.SCOPE_FIELD_ID;
import static org.o42a.core.object.type.Derivation.INHERITANCE;
import static org.o42a.core.object.type.Derivation.MEMBER_OVERRIDE;
import static org.o42a.core.object.type.Derivation.PROPAGATION;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class PropagationTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void overrideAncestor() {
		compile(
				"A := void (Foo := 1. Bar := foo ())",
				"B := a (Foo = 3)");

		final Obj aFoo = field(this.a, "foo").toObject();
		final Obj bFoo = field(this.b, "foo").toObject();

		assertThat(
				bFoo.type().derivedFrom(aFoo.type(), MEMBER_OVERRIDE),
				is(true));

		final Obj aBar = field(this.a, "bar").toObject();
		final Obj bBar = field(this.b, "bar").toObject();

		assertThat(definiteValue(bBar, ValueType.INTEGER), is(3L));
		assertThat(
				bBar.type().derivedFrom(aBar.type(), MEMBER_OVERRIDE),
				is(true));
		assertThat(
				bBar.type().derivedFrom(aFoo.type(), INHERITANCE),
				is(true));
		assertThat(
				bBar.type().derivedFrom(aFoo.type(), PROPAGATION),
				is(false));
		assertThat(
				bBar.type()
				.getAncestor().getType().type().derivedFrom(
						aFoo.type(),
						MEMBER_OVERRIDE),
				is(true));
		assertThat(bBar.type().inherits(bFoo.type()), is(true));

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
				"B := a (Foo = * (Bar = 2))");

		final Obj aFoo = field(this.a, "foo").toObject();
		final Obj bFoo = field(this.b, "foo").toObject();

		assertThat(
				bFoo.type().derivedFrom(aFoo.type(), MEMBER_OVERRIDE),
				is(true));

		final Obj aBar = field(aFoo, "bar").toObject();
		final Obj bBar = field(bFoo, "bar").toObject();

		assertThat(
				bBar.type().derivedFrom(aBar.type(), MEMBER_OVERRIDE),
				is(true));
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

		assertThat(aFoo.type().inherits(foo.type()), is(true));
		assertThat(aFoo.type().inherits(bar.type()), is(false));
		assertThat(bFoo.type().inherits(bar.type()), is(true));
		assertThat(bFoo.type().derivedFrom(aFoo.type()), is(true));

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
