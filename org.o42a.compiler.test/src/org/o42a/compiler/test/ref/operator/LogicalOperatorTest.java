/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class LogicalOperatorTest extends CompilerTestCase {

	@Test
	public void isTrue() {
		compile("A := ++1. B := ++false");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertTrueVoid(a);
		assertFalseVoid(b);
	}

	@Test
	public void not() {
		compile("A := --1. B := --false");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertFalseVoid(a);
		assertTrueVoid(b);
	}

}
