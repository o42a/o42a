/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class FieldRefTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void staticFieldRefInheritance() {
		compile(
				"A ::= void (Foo := 1)",
				"B := /a: foo");

		final Obj foo = field(this.a, "foo").toObject();

		assertThat(this.b.type().inherits(foo.type()), is(true));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void dynamicFieldRefInheritance() {
		compile(
				"A := void (Foo := 1)",
				"B := a: foo");

		final Obj foo = field(this.a, "foo").toObject();

		assertThat(this.b.type().inherits(foo.type()), is(true));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void enclosedFieldRef() {
		compile(
				"A := void (Foo := 1)",
				"B := void (Bar := a: foo)");

		final Obj foo = field(this.a, "foo").toObject();
		final Obj bar = field(this.b, "bar").toObject();

		assertThat(bar.type().inherits(foo.type()), is(true));
		assertThat(definiteValue(bar, ValueType.INTEGER), is(1L));
	}

	@Test
	public void overrideFieldRef() {
		compile(
				"A := void (Foo := 1)",
				"B := void (Bar := a: foo)",
				"C := b (*Bar (= 2))");

		final Obj c = field("c").toObject();

		final Obj foo = field(this.a, "foo").toObject();
		final Obj bar = field(c, "bar").toObject();

		assertThat(bar.type().derivedFrom(foo.type()), is(true));
		assertThat(definiteValue(bar, ValueType.INTEGER), is(2L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
	}

}
