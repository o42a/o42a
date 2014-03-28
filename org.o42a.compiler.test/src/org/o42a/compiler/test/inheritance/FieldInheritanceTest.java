/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class FieldInheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Test
	public void inheritField() {
		compile(
				"A := void (Foo := 123)",
				"B := a",
				"C := b");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertThat(bFoo.isPropagated(), is(true));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(123L));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertThat(cFoo.isPropagated(), is(true));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(123L));
	}

	@Test
	public void overrideField() {
		compile(
				"A := void (Foo := 123)",
				"B := a (Foo = 12)",
				"C := b");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertThat(bFoo.isPropagated(), is(false));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(12L));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertThat(cFoo.isPropagated(), is(true));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(12L));
	}

	@Test
	public void scopedOverride() {
		compile(
				"A := void (Foo := 123)",
				"B := a (Foo @a = 12)",
				"C := b");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(bFoo.getKey(), is(aFoo.getKey()));
		assertThat(bFoo.isPropagated(), is(false));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(12L));

		assertThat(cFoo.getKey(), is(aFoo.getKey()));
		assertThat(cFoo.isPropagated(), is(true));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(12L));
	}

	@Test
	public void overlapField() {
		compile(
				"A := void (Foo := 123)",
				"B := a (Foo := 12)",
				"C := b");

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(12L));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getMemberKey())));

		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(12L));
		assertThat(
				cFoo.getKey(),
				not(equalTo(this.c.member(aFoo.getKey()).getMemberKey())));
	}

	@Test
	public void hiddenOverride() {
		compile(
				"A := void (Foo := 123)",
				"Z := a (Foo := 321)",
				"B := z (Foo @a = 12)",
				"C := b");

		final Field aFoo = field(this.a, "foo");
		final Field z = field("z");
		final Field zFoo = field(z, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertThat(definiteValue(zFoo, ValueType.INTEGER), is(321L));

		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(321L));
		assertThat(
				bFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getMemberKey())));
		assertThat(bFoo.getKey(), is(zFoo.getKey()));
		assertThat(bFoo.isPropagated(), is(true));

		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(321L));
		assertThat(
				cFoo.getKey(),
				not(equalTo(this.b.member(aFoo.getKey()).getMemberKey())));
		assertThat(cFoo.getKey(), is(zFoo.getKey()));
		assertThat(cFoo.isPropagated(), is(true));

		final Field baFoo =
				this.b.member(aFoo.getKey()).toField().field(USE_CASE);

		assertThat(definiteValue(baFoo, ValueType.INTEGER), is(12L));
		assertThat(baFoo.isPropagated(), is(false));

		final Field caFoo =
				this.c.member(aFoo.getKey()).toField().field(USE_CASE);

		assertThat(definiteValue(caFoo, ValueType.INTEGER), is(12L));
		assertThat(caFoo.isPropagated(), is(true));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
		this.c = field("c").toObject();
	}

}
