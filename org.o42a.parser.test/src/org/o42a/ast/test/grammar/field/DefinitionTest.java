/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.field;

import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.declarator;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.StringSource;


public class DefinitionTest extends GrammarTestCase {

	@Test
	public void link() {

		final DeclaratorNode result = parse("foo := `bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertNull(result.getDefinitionType());
		assertThat(result.getDefinition(), isName("bar"));
	}

	@Test
	public void staticLink() {

		final DeclaratorNode result = parse("foo := `&bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertNull(result.getDefinitionType());

		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getDefinition());

		assertFalse(ascendants.hasSamples());
		assertThat(ascendants.getAncestor().getSpec(), isName("bar"));
	}

	@Test
	public void linkInterface() {

		final DeclaratorNode result = parse("foo := (`bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertThat(result.getDefinitionType(), isName("bar"));
		assertThat(result.getDefinition(), isName("baz"));
	}

	@Test
	public void staticLinkInterface() {

		final DeclaratorNode result = parse("foo := (`&bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());

		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getDefinitionType());

		assertThat(ascendants.getAncestor().getSpec(), isName("bar"));
		assertThat(result.getDefinition(), isName("baz"));
	}

	@Test
	public void variable() {

		final DeclaratorNode result = parse("foo := ``bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.VARIABLE, result.getDefinitionKind());
		assertNull(result.getDefinitionType());
		assertThat(result.getDefinition(), isName("bar"));
	}

	@Test
	public void variableInterface() {

		final DeclaratorNode result = parse("foo := (``bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.VARIABLE, result.getDefinitionKind());
		assertThat(result.getDefinitionType(), isName("bar"));
		assertThat(result.getDefinition(), isName("baz"));
	}

	private DeclaratorNode parse(String text) {
		this.worker = new ParserWorker(
				new StringSource(getClass().getSimpleName(), text));

		final MemberRefNode field =
				to(MemberRefNode.class, this.worker.parse(ref()));

		assertThat(field, isName("foo"));

		return this.worker.parse(declarator(field));
	}

}
