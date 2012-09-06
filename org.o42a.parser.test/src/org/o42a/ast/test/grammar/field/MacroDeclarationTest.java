/*
    Parser Tests
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
package org.o42a.ast.test.grammar.field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class MacroDeclarationTest extends GrammarTestCase {

	@Test
	public void declareMacro() {

		final DeclaratorNode declarator = to(
				DeclaratorNode.class,
				parse(
						DECLARATIVE.statement(),
						"#foo := bar"));
		final MacroExpansionNode declarable =
				to(MacroExpansionNode.class, declarator.getDeclarable());

		assertThat(declarable.getOperator(), is(UnaryOperator.MACRO_EXPANSION));
		assertThat(declarable.getOperand(), isName("foo"));
		assertThat(declarator.getDefinition(), isName("bar"));
		assertThat(declarator.getDefinitionKind(), nullValue());
		assertThat(declarator.getTarget(), is(DeclarationTarget.VALUE));
	}

	@Test
	public void overrideMacro() {

		final DeclaratorNode declarator = to(
				DeclaratorNode.class,
				parse(
						DECLARATIVE.statement(),
						"#foo = bar"));
		final MacroExpansionNode declarable =
				to(MacroExpansionNode.class, declarator.getDeclarable());

		assertThat(declarable.getOperator(), is(UnaryOperator.MACRO_EXPANSION));
		assertThat(declarable.getOperand(), isName("foo"));
		assertThat(declarator.getDefinition(), isName("bar"));
		assertThat(declarator.getDefinitionKind(), nullValue());
		assertThat(
				declarator.getTarget(),
				is(DeclarationTarget.OVERRIDE_VALUE));
	}

	@Test
	public void invalidMacroDeclaration() {
		expectError("syntax_error");

		to(
				MacroExpansionNode.class,
				parse(
						DECLARATIVE.statement(),
						"#(foo) := bar"));
	}

	@Test
	public void imperativeMacroNotSupported() {
		expectError("syntax_error");

		to(
				MacroExpansionNode.class,
				parse(
						IMPERATIVE.statement(),
						"#foo := bar"));
	}

}
