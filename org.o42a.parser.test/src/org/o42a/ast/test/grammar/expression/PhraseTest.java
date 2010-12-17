/*
    Parser Tests
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.junit.Assert.assertEquals;
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
	public void imperativeCall() {

		final PhraseNode result = parse("foo{bar}");

		assertName("foo", result.getPrefix());

		assertName(
				"bar",
				singleStatement(
						MemberRefNode.class,
						singleClause(BracesNode.class, result)));
	}

	@Test
	public void positionalCall() {

		final PhraseNode result = parse("foo[bar]");

		assertName("foo", result.getPrefix());

		final ArgumentNode[] arguments =
			singleClause(BracketsNode.class, result).getArguments();

		assertEquals(1, arguments.length);
		assertName("bar", arguments[0].getValue());
	}

	@Test
	public void qualifiedString() {

		final PhraseNode result = parse("foo 'bar' 'baz'");

		assertName("foo", result.getPrefix());

		final StringNode[] literals =
			singleClause(TextNode.class, result).getLiterals();

		assertEquals(2, literals.length);

		assertEquals("bar", literals[0].getText());
		assertEquals("baz", literals[1].getText());
	}

	@Test
	public void nameClause() {

		final PhraseNode result = parse("foo [bar] baz");

		assertName("foo", result.getPrefix());

		final ArgumentNode[] arguments =
			clause(BracketsNode.class, result, 0, 2).getArguments();

		assertEquals(1, arguments.length);
		assertName("bar", arguments[0].getValue());

		final NameNode name = clause(NameNode.class, result, 1, 2);

		assertEquals("baz", name.getName());
	}

	private PhraseNode parse(String text) {
		return to(PhraseNode.class, parse(DECLARATIVE.expression(), text));
	}

}
