/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.type.TypeArgumentSuffix.BACKQUOTE;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgNode;
import org.o42a.ast.type.TypeArgumentsNode;


public class TypeArgumentsTest extends GrammarTestCase {

	@Test
	public void argument() {

		final TypeArgumentsNode args = parse("foo` bar");

		assertThat(args.getArguments().length, is(1));

		final TypeArgNode arg = args.getArguments()[0];

		assertThat(arg.getArgument(), isName("foo"));
		assertThat(arg.getSuffix().getType(), is(BACKQUOTE));
		assertThat(args.getType(), isName("bar"));
	}

	@Test
	public void arguments() {

		final TypeArgumentsNode args = parse("(foo, bar)` baz");

		assertThat(args.getArguments().length, is(1));

		final TypeArgNode arg = args.getArguments()[0];
		final ParenthesesNode arguments =
				to(ParenthesesNode.class, arg.getArgument());

		assertThat(statement(RefNode.class, arguments, 0, 2), isName("foo"));
		assertThat(statement(RefNode.class, arguments, 1, 2), isName("bar"));
		assertThat(arg.getSuffix().getType(), is(BACKQUOTE));

		assertThat(args.getType(), isName("baz"));
	}

	@Test
	public void argumentOfArgument() {

		final TypeArgumentsNode args = parse("foo` bar` baz");

		assertThat(args.getArguments().length, is(2));

		assertThat(args.getArguments()[0].getArgument(), isName("foo"));
		assertThat(args.getArguments()[1].getArgument(), isName("bar"));
		assertThat(args.getType(), isName("baz"));
	}

	@Test
	public void argumentOfAscendants() {

		final TypeArgumentsNode args = parse("foo` bar & baz");

		assertThat(args.getArguments().length, is(1));

		final TypeArgNode arg = args.getArguments()[0];

		assertThat(arg.getArgument(), isName("foo"));
		assertThat(arg.getSuffix().getType(), is(BACKQUOTE));

		final AscendantsNode ascendants =
				to(AscendantsNode.class, args.getType());

		assertThat(ascendants.getSamples().length, is(1));
		assertThat(ascendants.getAncestor().getSpec(), isName("bar"));
		assertThat(ascendants.getSamples()[0].getSpec(), isName("baz"));
	}

	@Test
	public void phrasePrefix() {

		final PhraseNode phrase = to(
				PhraseNode.class,
				parse(simpleExpression(), "foo` bar [baz]"));
		final TypeArgumentsNode args =
				to(TypeArgumentsNode.class, phrase.getPrefix());

		assertThat(phrase.getParts().length, is(1));
		assertThat(args.getArguments().length, is(1));
		assertThat(args.getArguments()[0].getArgument(), isName("foo"));
		assertThat(args.getType(), isName("bar"));
	}

	private TypeArgumentsNode parse(String text) {
		return to(
				TypeArgumentsNode.class,
				parse(simpleExpression(), text));
	}

}
