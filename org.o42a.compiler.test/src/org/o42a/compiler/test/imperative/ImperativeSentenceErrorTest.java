/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeSentenceErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterUnconditionalReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False, = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void",
				"  = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterBracesWithReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  {False? Void. = 2}",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterParenthesesWithReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  (False? Void. = 2)",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  ...",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False, (...)",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void",
				"  ...",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  !",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False!",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void. False",
				"  False!",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterReturnAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  = 2; = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterRepeatAndReturnAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False, (...) ; = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterLoopAndRepeatAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False, (...) ; !",
				"  Void",
				"})");
	}

}
