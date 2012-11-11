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
		final PhrasePartNode[] clauses = phrase.getClauses();

		assertThat(phrase.getPrefix(), isName("a"));
		assertThat(clauses.length, is(1));

		final TypeDefinitionNode definition =
				to(TypeDefinitionNode.class, clauses[0]);

		assertThat(
				definition.getPrefix().getType(),
				is(TypeDefinitionNode.Prefix.HASH));
		assertThat(definition.getDefinition().getContent().length, is(1));
	}

	@Test
	public void ascendantsDefinition() {

		final PhraseNode phrase = parse("A & b #(Foo := bar)");
		final PhrasePartNode[] clauses = phrase.getClauses();
		final AscendantsNode ascendants =
				to(AscendantsNode.class, phrase.getPrefix());

		assertThat(ascendants.getAncestor().getSpec(), isName("a"));
		assertThat(ascendants.getSamples().length, is(1));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("b"));

		final TypeDefinitionNode definition =
				to(TypeDefinitionNode.class, clauses[0]);

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
