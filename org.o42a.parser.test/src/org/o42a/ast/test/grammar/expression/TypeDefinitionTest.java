/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;


public class TypeDefinitionTest extends GrammarTestCase {

	@Test
	public void typeDefinition() {

		final PhraseNode phrase = parse("A #(Foo := bar)");
		final PhrasePartNode[] parts = phrase.getParts();

		assertThat(phrase.getPrefix(), isName("a"));
		assertThat(parts.length, is(1));

		final TypeDefinitionNode definition =
				to(TypeDefinitionNode.class, parts[0]);

		assertThat(
				definition.getPrefix().getType(),
				is(TypeDefinitionNode.Prefix.HASH));
		assertThat(definition.getDefinition().getContent().length, is(1));
	}

	@Test
	public void ascendantsDefinition() {

		final PhraseNode phrase = parse("A & b #(Foo := bar)");
		final PhrasePartNode[] parts = phrase.getParts();
		final AscendantsNode ascendants =
				to(AscendantsNode.class, phrase.getPrefix());

		assertThat(ascendants.getAncestor().getSpec(), isName("a"));
		assertThat(ascendants.getSamples().length, is(1));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("b"));

		final TypeDefinitionNode definition =
				to(TypeDefinitionNode.class, parts[0]);

		assertThat(
				definition.getPrefix().getType(),
				is(TypeDefinitionNode.Prefix.HASH));
		assertThat(definition.getDefinition().getContent().length, is(1));

	}
	private PhraseNode parse(String text) {
		return to(
				PhraseNode.class,
				parse(simpleExpression(), text));
	}

}
