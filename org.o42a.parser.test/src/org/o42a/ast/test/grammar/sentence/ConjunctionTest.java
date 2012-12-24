/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.sentence;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ConjunctionTest extends GrammarTestCase {

	@Test
	public void oneStatement() {

		final SerialNode[] result = parse("~~ a ~~ foo ~~ b ");

		assertNotNull(result);
		assertEquals(1, result.length);
		assertThat(result[0].getStatement(), isName("foo"));
	}

	@Test
	public void twoStatements() {

		final SerialNode[] result = parse("foo, bar");

		assertNotNull(result);
		assertEquals(2, result.length);
		assertThat(result[0].getStatement(), isName("foo"));
		assertThat(result[1].getStatement(), isName("bar"));
	}

	@Test
	public void threeStatements() {

		final SerialNode[] result = parse("foo, bar, bas");

		assertNotNull(result);
		assertEquals(3, result.length);
		assertThat(result[0].getStatement(), isName("foo"));
		assertThat(result[1].getStatement(), isName("bar"));
		assertThat(result[2].getStatement(), isName("bas"));
	}

	@Test
	public void emptyConjunction() {

		final SerialNode[] result = parse(" ");

		assertThat(result, nullValue());
	}

	@Test
	public void onlyComment() {

		final SerialNode[] result = parse("~~ comment ~~");

		assertThat(result, nullValue());
	}

	private SerialNode[] parse(String text) {
		return parse(DECLARATIVE.conjunction(), text);
	}

}
