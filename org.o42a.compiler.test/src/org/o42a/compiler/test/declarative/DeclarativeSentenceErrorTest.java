/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class DeclarativeSentenceErrorTest extends CompilerTestCase {

	@Test
	public void redundantAfterUnconditionalValue() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  = 2",
				"  Void",
				")");
	}

	@Test
	public void redundantAfterConditionalValue() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  False, = 2",
				"  Void",
				")");
	}

	@Test
	public void redundantAfterCompoundValue() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  False? Void",
				"  = 2",
				"  Void",
				")");
	}

	@Test
	public void redundantAfterBlockWithValue() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  (False? Void. = 2)",
				"  Void",
				")");
	}

	@Test
	public void redundantAfterBracesWithReturn() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  {False? Void. = 2}",
				"  Void",
				")");
	}

	@Test
	public void redundantAfterValueAlts() {
		expectError("compiler.redundant_sentence");
		compile(
				"A := integer (",
				"  = 2; = 3",
				"  Void",
				")");
	}

}
