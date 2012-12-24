/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class PrototypeAccessTest extends CompilerTestCase {

	@Test
	public void indefiniteObjectInheritance() {
		expectError("compiler.cant_inherit");

		compile(
				"A :=> void (Foo := 1)",
				"B := a: foo");
	}

}
