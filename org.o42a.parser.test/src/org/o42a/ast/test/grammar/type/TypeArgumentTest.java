/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.type.TypeArgumentNode.TypeArgumentSeparator.BACKQUOTE;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentNode;


public class TypeArgumentTest extends GrammarTestCase {

	@Test
	public void argument() {

		final TypeArgumentNode result = parse("foo` bar");

		assertThat(result.getArgument(), isName("foo"));
		assertThat(result.getSeparator().getType(), is(BACKQUOTE));
		assertThat(result.getType(), isName("bar"));
	}

	@Test
	public void arguments() {

		final TypeArgumentNode result = parse("(foo, bar)` baz");
		final ParenthesesNode args =
				to(ParenthesesNode.class, result.getArgument());

		assertThat(statement(RefNode.class, args, 0, 2), isName("foo"));
		assertThat(statement(RefNode.class, args, 1, 2), isName("bar"));
		assertThat(result.getSeparator().getType(), is(BACKQUOTE));
		assertThat(result.getType(), isName("baz"));
	}

	@Test
	public void argumentOfArgument() {

		final TypeArgumentNode result = parse("foo` bar` baz");
		final TypeArgumentNode nested =
				to(TypeArgumentNode.class, result.getArgument());

		assertThat(nested.getArgument(), isName("foo"));
		assertThat(nested.getType(), isName("bar"));
		assertThat(result.getType(), isName("baz"));
	}

	@Test
	public void argumentOfAscendants() {

		final TypeArgumentNode result = parse("foo` bar & baz");

		assertThat(result.getArgument(), isName("foo"));
		assertThat(result.getSeparator().getType(), is(BACKQUOTE));

		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getType());

		assertThat(ascendants.getSamples().length, is(1));
		assertThat(ascendants.getAncestor().getSpec(), isName("bar"));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("baz"));
	}

	private TypeArgumentNode parse(String text) {
		return to(
				TypeArgumentNode.class,
				parse(simpleExpression(), text));
	}

}
