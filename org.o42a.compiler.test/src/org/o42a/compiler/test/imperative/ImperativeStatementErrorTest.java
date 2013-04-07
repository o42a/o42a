/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeStatementErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterReturn() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({= 1, void})");
	}

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({..., void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedRepeat() {
		expectError("compiler.unreachable_statement");
		compile("A := integer (Foo: {(... foo), void})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({(!), void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedExit() {
		expectError("compiler.unreachable_statement");
		compile("A := integer (Foo: {(... foo!), void})");
	}

	@Test
	public void unreachableAfterReturnAlts() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({(= 1; = 2), void})");
	}

	@Test
	public void unreachableAfterReturnAndRepeatAlts() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({(False, = 1; void, ...), void})");
	}

	@Test
	public void unreachableAfterExitAndRepeatAlts() {
		expectError("compiler.unreachable_statement");
		compile("A := integer ({(False, (!); void, ...), void})");
	}

}
