/*
    Parser Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class PhraseTest extends GrammarTestCase {

	@Test
	public void call() {

		final PhraseNode result = parse("foo(bar)");

		assertName("foo", result.getPrefix());

		assertName(
				"bar",
				singleStatement(
						MemberRefNode.class,
						singleClause(ParenthesesNode.class, result)));
	}

	@Test
	public void nlAfterCall() {

		final PhraseNode result = parse(
				"foo()",
				"bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(1));
	}

	@Test
	public void continuationAfterCall() {

		final PhraseNode result = parse(
				"foo()",
				"_ bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(2));
	}

	@Test
	public void imperative() {

		final PhraseNode result = parse("foo{bar}");

		assertName("foo", result.getPrefix());

		assertName(
				"bar",
				singleStatement(
						MemberRefNode.class,
						singleClause(BracesNode.class, result)));
	}

	@Test
	public void nlAfterImperative() {

		final PhraseNode result = parse(
				"foo{}",
				"bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(1));
	}

	@Test
	public void continuationAfterImperative() {

		final PhraseNode result = parse(
				"foo{}",
				"_ bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(2));
	}

	@Test
	public void value() {

		final PhraseNode result = parse("foo[bar]");

		assertName("foo", result.getPrefix());

		final ArgumentNode[] arguments =
				singleClause(BracketsNode.class, result).getArguments();

		assertEquals(1, arguments.length);
		assertName("bar", arguments[0].getValue());
	}

	@Test
	public void nlAfterValue() {

		final PhraseNode result = parse(
				"foo[]",
				"bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(1));
	}

	@Test
	public void continuationAfterValue() {

		final PhraseNode result = parse(
				"foo[]",
				"_ bar");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(2));
	}

	@Test
	public void string() {

		final PhraseNode result = parse("foo 'bar' 'baz'");

		assertName("foo", result.getPrefix());

		final StringNode[] literals =
				singleClause(TextNode.class, result).getLiterals();

		assertEquals(2, literals.length);

		assertEquals("bar", literals[0].getText());
		assertEquals("baz", literals[1].getText());
	}

	@Test
	public void nlAfterStrinf() {

		final PhraseNode result = parse(
				"foo 'bar'",
				"baz");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(1));
	}

	@Test
	public void continuationAfterString() {

		final PhraseNode result = parse(
				"foo 'bar'",
				"_ baz");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(2));
	}

	@Test
	public void name() {

		final PhraseNode result = parse("foo [bar] baz");

		assertName("foo", result.getPrefix());

		final ArgumentNode[] arguments =
				clause(BracketsNode.class, result, 0, 2).getArguments();

		assertEquals(1, arguments.length);
		assertName("bar", arguments[0].getValue());

		final NameNode name = clause(NameNode.class, result, 1, 2);

		assertEquals("baz", name.getName());
	}

	@Test
	public void argumentAfterName() {

		final PhraseNode result = parse("foo _ bar [baz]");

		assertName("foo", result.getPrefix());

		final NameNode name = clause(NameNode.class, result, 0, 2);

		assertEquals("bar", name.getName());

		final ArgumentNode[] arguments =
				clause(BracketsNode.class, result, 1, 2).getArguments();

		assertEquals(1, arguments.length);
		assertName("baz", arguments[0].getValue());
	}

	@Test
	public void names() {

		final PhraseNode result = parse("foo_bar_baz");

		assertName("foo", result.getPrefix());

		final NameNode name1 = clause(NameNode.class, result, 0, 2);

		assertEquals("bar", name1.getName());

		final NameNode name2 = clause(NameNode.class, result, 1, 2);

		assertEquals("baz", name2.getName());
	}

	@Test
	public void nlAfterName() {

		final PhraseNode result = parse(
				"foo _ bar",
				"(baz)");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(1));
	}

	@Test
	public void continuationAfterName() {

		final PhraseNode result = parse(
				"foo ",
				"_ bar",
				"_ (baz)");

		assertName("foo", result.getPrefix());
		assertThat(result.getClauses().length, is(2));
	}

	private PhraseNode parse(String... lines) {
		return to(PhraseNode.class, parseLines(
				DECLARATIVE.expression(),
				lines));
	}

}
