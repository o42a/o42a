/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class UnaryOperatorTest extends CompilerTestCase {

	@Test
	public void plus() {
		compile(
				"A :=> integer (<+*> = 1)",
				"B := + a");

		final Obj b = field("b").toObject();

		assertTrue(b.type().derivedFrom(
				this.context.getIntrinsics().getInteger().type()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void minus() {
		compile(
				"A :=> integer (<-*> = 1)",
				"B := - a");

		final Obj b = field("b").toObject();

		assertTrue(b.type().derivedFrom(
				this.context.getIntrinsics().getInteger().type()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void remainSamePlus() {
		compile(
				"A := integer (= 2. <+*>)",
				"B := + a");

		final Obj b = field("b").toObject();

		assertTrue(b.type().derivedFrom(
				this.context.getIntrinsics().getInteger().type()));
		assertEquals(2L, definiteValue(b));
	}

}
