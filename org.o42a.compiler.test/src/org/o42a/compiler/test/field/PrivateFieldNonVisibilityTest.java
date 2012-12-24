/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.field;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class PrivateFieldNonVisibilityTest extends CompilerTestCase {

	@Test
	public void inAnotherSource() {
		expectError("compiler.undefined_member");

		addSource(
				"a",
				"A := void",
				"=========",
				":Foo := 36");
		compile("B := a: foo");
	}

	@Test
	public void deepInAnotherSource() {
		expectError("compiler.undefined_member");

		addSource(
				"a",
				"A := void",
				"=========",
				"C := b: foo");
		addSource(
				"a/b",
				"B := void",
				"=========");
		addSource(
				"a/b/foo",
				":Foo := 64",
				"=========");
		compile("");
	}

	@Test
	public void derive() {
		expectError("compiler.unresolved");

		compile(
				"A := void (",
				"  :Foo := 1",
				")",
				"B := a (",
				"  Bar := foo",
				")");
	}

	@Test
	public void override() {
		expectError("compiler.cant_override_unknown");

		compile(
				"A := void (",
				"  :Foo := 1",
				")",
				"B := a (",
				"  foo = 2",
				")");
	}

}
