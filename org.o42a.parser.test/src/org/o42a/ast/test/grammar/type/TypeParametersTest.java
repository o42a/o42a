/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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


public class TypeParametersTest extends GrammarTestCase {

	@Test
	public void linkTypeParameter() {

		final TypeParametersNode result = parse("Foo (`bar)");

		assertThat(result.getType(), isName("foo"));
		assertThat(result.getParameters().getParameters().length, is(1));
		assertThat(
				result.getParameters().getParameters()[0].getType(),
				isName("bar"));
		assertThat(
				result.getParameters().getKind().getType(),
				is(DefinitionKind.LINK));
		assertThat(
				result.getParameters().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getParameters().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void linkTypeParameters() {

		final TypeParametersNode result = parse("Foo (`bar, baz)");

		assertThat(result.getType(), isName("foo"));
		assertThat(result.getParameters().getParameters().length, is(2));
		assertThat(
				result.getParameters().getParameters()[0].getType(),
				isName("bar"));
		assertThat(
				result.getParameters().getParameters()[1].getType(),
				isName("baz"));
		assertThat(
				result.getParameters().getKind().getType(),
				is(DefinitionKind.LINK));
		assertThat(
				result.getParameters().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getParameters().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void variableTypeParameter() {

		final TypeParametersNode result = parse("Foo (``bar)");

		assertThat(result.getType(), isName("foo"));
		assertThat(result.getParameters().getParameters().length, is(1));
		assertThat(
				result.getParameters().getParameters()[0].getType(),
				isName("bar"));
		assertThat(
				result.getParameters().getKind().getType(),
				is(DefinitionKind.VARIABLE));
		assertThat(
				result.getParameters().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getParameters().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void staticTypeParameter() {

		final TypeParametersNode result = parse("&Foo (`bar)");
		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getType());

		assertFalse(ascendants.hasSamples());
		assertThat(
				ascendants.getAncestor().getSeparator().getType(),
				is(Separator.SAMPLE));
		assertThat(ascendants.getAncestor().getSpec(), isName("foo"));

		assertThat(result.getParameters().getParameters().length, is(1));
		assertThat(
				result.getParameters().getParameters()[0].getType(),
				isName("bar"));
		assertThat(
				result.getParameters().getKind().getType(),
				is(DefinitionKind.LINK));
		assertThat(
				result.getParameters().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getParameters().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	@Test
	public void ascendantsParameter() {

		final TypeParametersNode result = parse("Foo & bar (``baz)");
		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getType());

		assertThat(ascendants.getSamples().length, is(1));
		assertThat(
				ascendants.getAncestor().getSeparator(),
				nullValue());
		assertThat(ascendants.getAncestor().getSpec(), isName("foo"));
		assertThat(
				ascendants.getSamples()[0].getSeparator().getType(),
				is(Separator.SAMPLE));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("bar"));

		assertThat(result.getParameters().getParameters().length, is(1));
		assertThat(
				result.getParameters().getParameters()[0].getType(),
				isName("baz"));
		assertThat(
				result.getParameters().getKind().getType(),
				is(DefinitionKind.VARIABLE));
		assertThat(
				result.getParameters().getOpening().getType(),
				is(ParenthesisSign.OPENING_PARENTHESIS));
		assertThat(
				result.getParameters().getClosing().getType(),
				is(ParenthesisSign.CLOSING_PARENTHESIS));
	}

	private TypeParametersNode parse(String text) {
		return to(
				TypeParametersNode.class,
				parse(simpleExpression(), text));
	}

}
