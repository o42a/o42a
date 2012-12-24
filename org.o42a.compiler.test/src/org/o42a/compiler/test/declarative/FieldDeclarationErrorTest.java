/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class FieldDeclarationErrorTest extends CompilerTestCase {

	@Test
	public void fieldInsideIssue() {
		expectError("compiler.prohibited_issue_field");
		compile(
				"A := integer (",
				"  Foo := 2?",
				")");
	}

	@Test
	public void fieldDeepInsideIssue() {
		expectError("compiler.prohibited_issue_field");
		compile(
				"A := integer (",
				"  (False, (Void, Bar := 2))?",
				")");
	}

	@Test
	public void fieldInsideClaim() {
		expectError("compiler.prohibited_claim_field");
		compile("A := 1!");
	}

	@Test
	public void fieldDeepInsideClaim() {
		expectError("compiler.prohibited_claim_field");
		compile("((A := 1))!");
	}

}
