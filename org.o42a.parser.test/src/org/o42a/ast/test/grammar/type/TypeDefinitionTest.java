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
package org.o42a.ast.test.grammar.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeDefinitionNode;


public class TypeDefinitionTest extends GrammarTestCase {

	@Test
	public void typeDefinition() {

		final TypeDefinitionNode result = parse("A #(Foo := bar)");

		assertThat(result.getType(), isName("a"));
		assertThat(
				result.getPrefix().getType(),
				is(TypeDefinitionNode.Prefix.HASH));
		assertThat(result.getDefinition().getContent().length, is(1));
	}

	@Test
	public void ascendantsDefinition() {

		final TypeDefinitionNode result = parse("A & b # (Foo := bar)");
		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getType());

		assertThat(ascendants.getAncestor().getSpec(), isName("a"));
		assertThat(ascendants.getSamples().length, is(1));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("b"));
		assertThat(
				result.getPrefix().getType(),
				is(TypeDefinitionNode.Prefix.HASH));
		assertThat(result.getDefinition().getContent().length, is(1));

	}
	private TypeDefinitionNode parse(String text) {
		return to(
				TypeDefinitionNode.class,
				parse(simpleExpression(), text));
	}

}
