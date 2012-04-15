package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class LocalFieldErrorTest extends CompilerTestCase {

	@Test
	public void fieldInsideIssue() {
		expectError("compiler.prohibited_issue_field");
		compile(
				"A := integer ({",
				"  Foo := 2?",
				"})");
	}

	@Test
	public void fieldDeepInsideIssue() {
		expectError("compiler.prohibited_issue_field");
		compile(
				"A := integer ({",
				"  (False, (Void, Bar := 2))?",
				"})");
	}

}
