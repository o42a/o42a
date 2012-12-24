/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class FieldOfPrototypeRefTest extends CompilerTestCase {

	@Test
	public void indefiniteObjectValue() {
		expectError("compiler.not_object");

		compile(
				"A :=> void (Foo := 1)",
				"B := integer (= a: foo)");
	}

	@Test
	public void indefiniteObjectCondition() {
		expectError("compiler.not_object");

		compile(
				"A :=> void (Foo := 1)",
				"B := integer (a: foo)");
	}

}
