/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class MisplacedImperativeStatementTest extends CompilerTestCase {

	@Test
	public void emptyInterrogativeSentence() {
		expectError("compiler.prohibited_empty_interrogative_sentence");
		compile(
				"A := integer ({",
				"  ? = 2",
				"})");
	}

	@Test
	public void emptyCompundInterrogativeSentence() {
		expectError("compiler.prohibited_empty_interrogative_sentence");
		compile(
				"A := integer ({",
				"  (())? = 2",
				"})");
	}

	@Test
	public void returnFromInterrogatveSentence() {
		expectError("compiler.prohibited_interrogative_assignment");
		compile(
				"A := integer ({",
				"  = 2?",
				"})");
	}

	@Test
	public void returnFromInterrogation() {
		expectError("compiler.prohibited_interrogative_assignment");
		compile(
				"A := integer ({",
				"  (False, (Void, = 2))?",
				"})");
	}

	@Test
	public void repeatInInterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_repeat");
		compile(
				"A := integer ({",
				"  ...?",
				"})");
	}

	@Test
	public void repeatInInterrogation() {
		expectError("compiler.prohibited_interrogative_repeat");
		compile(
				"A := integer ({",
				"  (False; (Void...))?",
				"})");
	}

	@Test
	public void unconditionalExitFromIterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_exit");
		compile(
				"A := integer ({",
				"  (!)?",
				"})");
	}

	@Test
	public void conditionalExitFromIterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_exit");
		compile(
				"A := integer ({",
				"  (False!)?",
				"})");
	}

	@Test
	public void bracesInInterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_braces");
		compile(
				"A := integer ({",
				"  {}?",
				"})");
	}

}
