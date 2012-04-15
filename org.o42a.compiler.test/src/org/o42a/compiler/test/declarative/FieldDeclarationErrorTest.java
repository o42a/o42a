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
