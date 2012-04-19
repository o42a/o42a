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


public class DeclarativeSentenceErrorTest extends CompilerTestCase {

	@Test
	public void statementBeforeField() {
		expectError("compiler.not_alone_field");
		compile("Void; a := 1");
	}

	@Test
	public void statementAfterField() {
		expectError("compiler.not_alone_field");
		compile("A := 1; void");
	}

	@Test
	public void twoFields() {
		expectError("compiler.not_alone_field");
		compile("A := 1; b := 2");
	}

	@Test
	public void statementBeforeClause() {
		expectError("compiler.not_alone_clause");
		compile("Void; <a> false");
	}

	@Test
	public void statementAfterClause() {
		expectError("compiler.not_alone_clause");
		compile("<A> false; void");
	}

	@Test
	public void twoClauses() {
		expectError("compiler.not_alone_clause");
		compile("<A> false; <b> false");
	}

}
