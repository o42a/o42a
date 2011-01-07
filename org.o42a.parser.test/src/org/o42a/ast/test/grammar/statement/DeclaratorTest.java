/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.statement;

import static org.junit.Assert.assertEquals;
import static org.o42a.ast.statement.DeclarationTarget.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.statement.DeclarableAdapterNode;
import org.o42a.ast.statement.DeclarationTarget;
import org.o42a.ast.statement.DeclaratorNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class DeclaratorTest extends GrammarTestCase {

	@Test
	public void value() {
		assertDeclaration(OVERRIDE_VALUE, "=");
		assertDeclaration(VALUE, ":=");
	}

	@Test
	public void inputValue() {
		assertDeclaration(OVERRIDE_INPUT, "=<");
		assertDeclaration(INPUT, ":=<");
	}

	@Test
	public void prototype() {
		assertDeclaration(OVERRIDE_PROTOTYPE, "=>");
		assertDeclaration(PROTOTYPE, ":=>");
	}

	@Test
	public void abstractPrototype() {
		assertDeclaration(OVERRIDE_ABSTRACT, "=<>");
		assertDeclaration(ABSTRACT, ":=<>");
	}

	@Test
	public void valueAdapter() {
		assertAdapter(OVERRIDE_VALUE, "=");
		assertAdapter(VALUE, ":=");
	}

	@Test
	public void inputValueAdapter() {
		assertAdapter(OVERRIDE_INPUT, "=<");
		assertAdapter(INPUT, ":=<");
	}

	@Test
	public void prototypeAdapter() {
		assertAdapter(OVERRIDE_PROTOTYPE, "=>");
		assertAdapter(PROTOTYPE, ":=>");
	}

	@Test
	public void abstractPrototypeAdapter() {
		assertAdapter(OVERRIDE_ABSTRACT, "=<>");
		assertAdapter(ABSTRACT, ":=<>");
	}

	private void assertDeclaration(DeclarationTarget target, String sign) {

		final DeclaratorNode declarator = parse("foo " + sign + " bar ");

		assertEquals(target, declarator.getTarget());
		assertRange(0, 3, declarator.getDeclarable());
		assertRange(0, 8 + sign.length(), declarator);
		assertRange(
				4,
				4 + sign.length(),
				declarator.getDefinitionAssignment());
		assertName("foo", declarator.getDeclarable());
		assertName("bar", declarator.getDefinition());
	}

	private void assertAdapter(DeclarationTarget target, String sign) {

		final DeclaratorNode declarator = parse("@foo " + sign + " bar ");
		final DeclarableAdapterNode adapter =
			to(DeclarableAdapterNode.class, declarator.getDeclarable());

		assertRange(0, 4, adapter);
		assertRange(0, 1, adapter.getPrefix());
		assertRange(1, 4, adapter.getMember());
		assertEquals(target, declarator.getTarget());
		assertRange(0, 9 + sign.length(), declarator);
		assertRange(
				5,
				5 + sign.length(),
				declarator.getDefinitionAssignment());
		assertName("foo", adapter.getMember());
		assertName("bar", declarator.getDefinition());
	}

	private DeclaratorNode parse(String text) {
		return to(DeclaratorNode.class, parse(DECLARATIVE.statement(), text));
	}

}
