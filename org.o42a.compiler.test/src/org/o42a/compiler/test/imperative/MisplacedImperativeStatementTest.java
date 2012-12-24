/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class MisplacedImperativeStatementTest extends CompilerTestCase {

	@Test
	public void emptyIssue() {
		expectError("compiler.prohibited_empty_issue");
		compile(
				"A := integer ({",
				"  ? = 2",
				"})");
	}

	@Test
	public void emptyCompundIssue() {
		expectError("compiler.prohibited_empty_issue");
		compile(
				"A := integer ({",
				"  (())? = 2",
				"})");
	}

	@Test
	public void returnInsideIssue() {
		expectError("compiler.prohibited_issue_assignment");
		compile(
				"A := integer ({",
				"  = 2?",
				"})");
	}

	@Test
	public void returnDeepInsideIssue() {
		expectError("compiler.prohibited_issue_assignment");
		compile(
				"A := integer ({",
				"  (False, (Void, = 2))?",
				"})");
	}

	@Test
	public void repeatInsideIssue() {
		expectError("compiler.prohibited_issue_ellipsis");
		compile(
				"A := integer ({",
				"  ...?",
				"})");
	}

	@Test
	public void repeatDeepInsideIssue() {
		expectError("compiler.prohibited_issue_ellipsis");
		compile(
				"A := integer ({",
				"  (False; (Void...))?",
				"})");
	}

	@Test
	public void unconditionalExitInsideIssue() {
		expectError("compiler.prohibited_issue_exit");
		compile(
				"A := integer ({",
				"  (!)?",
				"})");
	}

	@Test
	public void conditionalExitInsideIssue() {
		expectError("compiler.prohibited_issue_exit");
		compile(
				"A := integer ({",
				"  (False!)?",
				"})");
	}

	@Test
	public void braceInsideIssue() {
		expectError("compiler.prohibited_issue_braces");
		compile(
				"A := integer ({",
				"  {}?",
				"})");
	}

}
