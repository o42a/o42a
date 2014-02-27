/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeAltErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer({(...); void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedRepeat() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer(Foo: {(... foo); void})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer({(!); void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedExit() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer(Foo: {(!.. foo); void})");
	}

}
