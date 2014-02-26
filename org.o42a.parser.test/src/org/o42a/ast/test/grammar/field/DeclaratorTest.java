/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.field.DeclarationTarget.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
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
	public void staticField() {
		assertDeclaration(STATIC, "::=");
	}

	@Test
	public void staticPrototype() {
		assertDeclaration(STATIC_PROTOTYPE, "::=>");
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

	private void assertDeclaration(DeclarationTarget target, String sign) {

		final DeclaratorNode declarator = parse("foo " + sign + " bar ");

		assertEquals(target, declarator.getTarget());
		assertThat(declarator.getDeclarable(), hasRange(0, 3));
		assertThat(declarator, hasRange(0, (long) (8 + sign.length())));
		assertThat(declarator.getDefinitionAssignment(), hasRange(4, (long) (4 + sign.length())));
		assertThat(declarator.getDeclarable(), isName("foo"));
		assertThat(declarator.getDefinition(), isName("bar"));
	}

	private void assertAdapter(DeclarationTarget target, String sign) {

		final DeclaratorNode declarator = parse("@foo " + sign + " bar ");
		final DeclarableAdapterNode adapter =
				to(DeclarableAdapterNode.class, declarator.getDeclarable());

		assertThat(adapter, hasRange(0, 4));
		assertThat(adapter.getPrefix(), hasRange(0, 1));
		assertThat(adapter.getMember(), hasRange(1, 4));
		assertEquals(target, declarator.getTarget());
		assertThat(declarator, hasRange(0, (long) (9 + sign.length())));
		assertThat(declarator.getDefinitionAssignment(), hasRange(5, (long) (5 + sign.length())));
		assertThat(adapter.getMember(), isName("foo"));
		assertThat(declarator.getDefinition(), isName("bar"));
	}

	private DeclaratorNode parse(String text) {
		return to(DeclaratorNode.class, parse(DECLARATIVE.statement(), text));
	}

}
