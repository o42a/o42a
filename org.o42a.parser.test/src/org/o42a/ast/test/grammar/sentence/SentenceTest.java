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
package org.o42a.ast.test.grammar.sentence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.sentence.*;
import org.o42a.ast.sentence.AlternativeNode.Separator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class SentenceTest extends GrammarTestCase {

	@Test
	public void sentence() {

		final SentenceNode sentence = parse("foo1, bar1; foo2, bar2");

		assertNotNull(sentence);
		assertEquals(SentenceType.PROPOSITION, sentence.getType());
		assertNull(sentence.getMark());

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		assertEquals(2, disjunction.length);

		final AlternativeNode alt1 = disjunction[0];

		assertNull(alt1.getSeparator());
		assertFalse(alt1.isOpposite());
		assertEquals(2, alt1.getConjunction().length);
		assertName("foo1", alt1.getConjunction()[0].getStatement());
		assertName("bar1", alt1.getConjunction()[1].getStatement());

		final AlternativeNode alt2 = disjunction[1];

		assertEquals(Separator.ALTERNATIVE, alt2.getSeparator().getType());
		assertFalse(alt2.isOpposite());
		assertEquals(2, alt2.getConjunction().length);
		assertName("foo2", alt2.getConjunction()[0].getStatement());
		assertName("bar2", alt2.getConjunction()[1].getStatement());
	}

	@Test
	public void opposite() {

		final SentenceNode sentence = parse("foo1, bar1 | foo2, bar2");

		assertNotNull(sentence);
		assertEquals(SentenceType.PROPOSITION, sentence.getType());
		assertNull(sentence.getMark());

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		assertEquals(2, disjunction.length);

		final AlternativeNode alt1 = disjunction[0];

		assertNull(alt1.getSeparator());
		assertFalse(alt1.isOpposite());
		assertEquals(2, alt1.getConjunction().length);
		assertName("foo1", alt1.getConjunction()[0].getStatement());
		assertName("bar1", alt1.getConjunction()[1].getStatement());

		final AlternativeNode alt2 = disjunction[1];

		assertEquals(Separator.OPPOSITE, alt2.getSeparator().getType());
		assertTrue(alt2.isOpposite());
		assertEquals(2, alt2.getConjunction().length);
		assertName("foo2", alt2.getConjunction()[0].getStatement());
		assertName("bar2", alt2.getConjunction()[1].getStatement());
	}

	@Test
	public void issue() {
		assertType(SentenceType.ISSUE, "foo?");
	}

	@Test
	public void proposition() {
		assertType(SentenceType.PROPOSITION, "foo.");
	}

	@Test
	public void claim() {
		assertType(SentenceType.CLAIM, "foo!");
	}

	@Test
	public void noSeparator() {
		assertType(SentenceType.PROPOSITION, "foo");
	}

	@Test
	public void multiLineConjunction() {

		final SentenceNode result = parse(
				"a,",
				"b,",
				"c");

		assertThat(result.getDisjunction()[0].getConjunction().length, is(3));
	}

	@Test
	public void multiLineDisjunction() {

		final SentenceNode result = parse(
				"a;",
				"b;",
				"c");

		assertThat(result.getDisjunction().length, is(3));
	}

	private SentenceNode assertType(SentenceType type, String text) {

		final SentenceNode sentence = parse(text);

		assertEquals(type, sentence.getType());

		return sentence;
	}

	private SentenceNode parse(String... lines) {
		return parseLines(DECLARATIVE.sentence(), lines);
	}

}
