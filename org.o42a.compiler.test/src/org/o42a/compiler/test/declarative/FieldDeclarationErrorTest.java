/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class FieldDeclarationErrorTest extends CompilerTestCase {

	@Test
	public void fieldInsideInterrogativeSentence() {
		expectError("compiler.prohibited_interrogative_field");
		compile(
				"A := integer (",
				"  Foo := 2?",
				")");
	}

	@Test
	public void fieldInsideInterrogation() {
		expectError("compiler.prohibited_interrogative_field");
		compile(
				"A := integer (",
				"  (False, (Void, Bar := 2))?",
				")");
	}

}
