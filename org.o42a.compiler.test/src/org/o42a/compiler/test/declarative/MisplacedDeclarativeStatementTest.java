/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class MisplacedDeclarativeStatementTest extends CompilerTestCase {

	@Test
	public void emptyIssue() {
		expectError("compiler.prohibited_empty_issue");
		compile(
				"A := integer (",
				"  ? = 2",
				")");
	}

	@Test
	public void emptyCompoundIssue() {
		expectError("compiler.prohibited_empty_issue");
		compile(
				"A := integer (",
				"  (())? = 2",
				")");
	}

	@Test
	public void selfAssignInsideIssue() {
		expectError("compiler.prohibited_issue_assignment");
		compile(
				"A := integer (",
				"  = 2?",
				")");
	}

	@Test
	public void selfAssignDeepInsideIssue() {
		expectError("compiler.prohibited_issue_assignment");
		compile(
				"A := integer (",
				"  (False, (Void, = 2))?",
				")");
	}

	@Test
	public void emptyClaimInsideIssue() {
		expectError("compiler.prohibited_issue_claim");
		compile(
				"A := integer (",
				"  (!)?",
				")");
	}

	@Test
	public void claimInsideIssue() {
		expectError("compiler.prohibited_issue_claim");
		compile(
				"A := integer (",
				"  (False!)?",
				")");
	}

	@Test
	public void braceInsideIssue() {
		expectError("compiler.prohibited_issue_braces");
		compile(
				"A := integer (",
				"  {}?",
				")");
	}

}
