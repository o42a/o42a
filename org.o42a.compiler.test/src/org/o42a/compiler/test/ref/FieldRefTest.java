/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class FieldRefTest extends CompilerTestCase {

	private Obj a;
	private Obj b;

	@Test
	public void staticFieldRefInheritance() {
		compile(
				"A := void (Foo := 1)",
				"B := &a: foo");

		final Obj foo = field(this.a, "foo").toObject();

		assertTrue(this.b.type().inherits(foo.type()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void dynamicFieldRefInheritance() {
		compile(
				"A := void (Foo := 1)",
				"B := a: foo");

		final Obj foo = field(this.a, "foo").toObject();

		assertTrue(this.b.type().inherits(foo.type()));
		assertEquals(definiteValue(this.b), 1L);
	}

	@Test
	public void enclosedFieldRef() {
		compile(
				"A := void (Foo := 1)",
				"B := void (Bar := a: foo)");

		final Obj foo = field(this.a, "foo").toObject();
		final Obj bar = field(this.b, "bar").toObject();

		assertTrue(bar.type().inherits(foo.type()));
		assertEquals(definiteValue(bar), 1L);
	}

	@Test
	public void overrideFieldRef() {
		compile(
				"A := void (Foo := 1)",
				"B := void (Bar := a: foo)",
				"C := b (Bar = a: foo (= 2))");

		final Obj c = field("c").toObject();

		final Obj foo = field(this.a, "foo").toObject();
		final Obj bar = field(c, "bar").toObject();

		assertTrue(bar.type().derivedFrom(foo.type()));
		assertEquals(definiteValue(bar), 2L);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
	}

}
