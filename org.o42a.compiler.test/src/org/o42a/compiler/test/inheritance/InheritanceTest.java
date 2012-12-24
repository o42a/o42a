/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.o42a.core.object.type.Derivation.MEMBER_OVERRIDE;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.junit.Before;
import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class InheritanceTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Before
	public void setUp() {
		compile(
				"A := integer(= 1. Foo := 123456).",
				"B := a(= 2. Foo = 1234567).",
				"C := b().");
		this.a = field("a").toObject();
		this.b = field("b").toObject();
		this.c = field("c").toObject();
	}

	@Test
	public void inheritance() {
		assertThat(
				this.b.type()
				.getAncestor()
				.getType()
				.getScope()
				.toField()
				.getKey()
				.getMemberName()
				.getName(),
				is(CASE_INSENSITIVE.canonicalName("a")));
		assertSame(
				this.a,
				this.b.type().getAncestor().getType());
		assertTrue(this.b.type().inherits(
				this.a.type()));
		assertSame(
				this.b,
				this.c.type().getAncestor().getType());
		assertTrue(this.c.type().inherits(this.a.type()));
		assertTrue(this.c.type().inherits(this.b.type()));
	}

	@Test
	public void value() {
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.c, ValueType.INTEGER), is(2L));
	}

	@Test
	public void fieldDeclaration() {

		final Field aFoo = field(this.a, "foo");

		assertThat(aFoo, notNullValue());
		assertFalse(aFoo.isPropagated());
		assertThat(
				aFoo.toObject().type()
				.getAncestor().getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void fieldOverride() {

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");

		assertFalse(bFoo.isPropagated());
		assertTrue(
				bFoo.toObject().type()
				.derivedFrom(
						aFoo.toObject().type(),
						MEMBER_OVERRIDE));
	}

	@Test
	public void fieldPropagation() {

		final Field aFoo = field(this.a, "foo");
		final Field bFoo = field(this.b, "foo");
		final Field cFoo = field(this.c, "foo");

		assertTrue(cFoo.isPropagated());
		assertTrue(
				cFoo.toObject().type()
				.derivedFrom(
						aFoo.toObject().type(),
						MEMBER_OVERRIDE));
		assertTrue(
				cFoo.toObject().type()
				.derivedFrom(
						bFoo.toObject().type(),
						MEMBER_OVERRIDE));
	}

}
