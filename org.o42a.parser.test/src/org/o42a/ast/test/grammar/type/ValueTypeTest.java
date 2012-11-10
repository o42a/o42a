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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantNode.Separator;
import org.o42a.ast.type.*;


public class ValueTypeTest extends GrammarTestCase {

	@Test
	public void linkValueType() {

		final TypeParametersNode result = parse("Foo (`bar)");

		assertThat(result.getAscendant(), isName("foo"));
		assertThat(result.getValueType().getType(), isName("bar"));
		assertThat(
				result.getValueType().getKind().getType(),
				is(DefinitionKind.LINK));
		assertThat(
				result.getValueType().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getValueType().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void variableValueType() {

		final TypeParametersNode result = parse("Foo (``bar)");

		assertThat(result.getAscendant(), isName("foo"));
		assertThat(result.getValueType().getType(), isName("bar"));
		assertThat(
				result.getValueType().getKind().getType(),
				is(DefinitionKind.VARIABLE));
		assertThat(
				result.getValueType().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getValueType().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void staticTypeValueType() {

		final TypeParametersNode result = parse("&Foo (`bar)");
		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getAscendant());

		assertFalse(ascendants.hasSamples());
		assertThat(
				ascendants.getAncestor().getSeparator().getType(),
				is(Separator.SAMPLE));
		assertThat(ascendants.getAncestor().getSpec(), isName("foo"));

		assertThat(result.getValueType().getType(), isName("bar"));
		assertThat(
				result.getValueType().getKind().getType(),
				is(DefinitionKind.LINK));
		assertThat(
				result.getValueType().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getValueType().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void ascendantsValueType() {

		final TypeParametersNode result = parse("Foo & bar (``baz)");
		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getAscendant());

		assertThat(ascendants.getSamples().length, is(1));
		assertThat(
				ascendants.getAncestor().getSeparator(),
				nullValue());
		assertThat(ascendants.getAncestor().getSpec(), isName("foo"));
		assertThat(
				ascendants.getSamples()[0].getSeparator().getType(),
				is(Separator.SAMPLE));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("bar"));

		assertThat(result.getValueType().getType(), isName("baz"));
		assertThat(
				result.getValueType().getKind().getType(),
				is(DefinitionKind.VARIABLE));
		assertThat(
				result.getValueType().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getValueType().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	private TypeParametersNode parse(String text) {
		return to(
				TypeParametersNode.class,
				parse(simpleExpression(), text));
	}

}
