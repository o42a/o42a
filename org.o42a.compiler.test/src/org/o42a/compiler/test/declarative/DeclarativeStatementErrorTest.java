/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class DeclarativeStatementErrorTest extends CompilerTestCase {

	@Test
	public void conditionAfterValue() {
		expectError("compiler.unreachable_statement");
		compile(
				"A := integer (",
				"  = 1,",
				"  false",
				")");
	}

	@Test
	public void conditionAfterBlockWithValue() {
		expectError("compiler.unreachable_statement");
		compile(
				"A := integer (",
				"  (= 2),",
				"  false",
				")");
	}

	@Test
	public void ambiguousSequentialValue() {
		expectError("compiler.unreachable_statement");
		compile(
				"A := integer (",
				"  = 1,",
				"  = 2",
				")");
	}

	@Test
	public void statementBeforeField() {
		expectError("compiler.not_alone_field");
		compile("Void, a := 1");
	}

	@Test
	public void statementAfterField() {
		expectError("compiler.not_alone_field");
		compile("A := 1, void");
	}

	@Test
	public void twoFields() {
		expectError("compiler.not_alone_field");
		compile("A := 1, b := 2");
	}

	@Test
	public void statementBeforeClause() {
		expectError("compiler.not_alone_clause");
		compile("Void, <a> false");
	}

	@Test
	public void statementAfterClause() {
		expectError("compiler.not_alone_clause");
		compile("<A> false, void");
	}

	@Test
	public void twoClauses() {
		expectError("compiler.not_alone_clause");
		compile("<A> false, <b> false");
	}

}
