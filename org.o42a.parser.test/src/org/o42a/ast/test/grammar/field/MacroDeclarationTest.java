/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
