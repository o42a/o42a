/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class MisplacedDeclarativeStatementTest extends CompilerTestCase {

	@Test
	public void emptyInterrogativeSentence() {
		expectError("compiler.prohibited_empty_interrogative_sentence");
		compile(
				"A := integer (",
				"  ? = 2",
				")");
	}

	@Test
	public void emptyCompoundInterrogativeSentence() {
		expectError("compiler.prohibited_empty_interrogative_sentence");
		compile(
				"A := integer (",
				"  (())? = 2",
				")");
	}

	@Test
	public void selfAssignInInterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_assignment");
		compile(
				"A := integer (",
				"  = 2?",
				")");
	}

	@Test
	public void selfAssignInsideInterrogation() {
		expectError("compiler.prohibited_interrogative_assignment");
		compile(
				"A := integer (",
				"  (False, (Void, = 2))?",
				")");
	}

	@Test
	public void exitFromInterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_exit");
		compile(
				"A := integer (",
				"  (!)?",
				")");
	}

	@Test
	public void exitFromInterrogation() {
		expectError("compiler.prohibited_interrogative_exit");
		compile(
				"A := integer (",
				"  (False!)?",
				")");
	}

	@Test
	public void bracesInsideInterogativeSentence() {
		expectError("compiler.prohibited_interrogative_braces");
		compile(
				"A := integer (",
				"  {}?",
				")");
	}

}
