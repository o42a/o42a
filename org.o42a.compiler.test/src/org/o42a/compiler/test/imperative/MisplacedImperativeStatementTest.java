/*
    Compiler Tests
    Copyright (C) 2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
