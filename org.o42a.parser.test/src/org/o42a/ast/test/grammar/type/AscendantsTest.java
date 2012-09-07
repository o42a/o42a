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
package org.o42a.ast.test.grammar.type;

import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantNode;
import org.o42a.ast.type.AscendantsNode;


public class AscendantsTest extends GrammarTestCase {

	@Test
	public void sample() {

		final AscendantsNode result = parse("&foo");

		assertThat(result, hasRange(0, 4));
		assertFalse(result.hasSamples());
		assertNotNull(result.getAncestor().getSeparator());
		assertThat(result.getAncestor(), hasRange(0, 4));
		assertThat(result.getAncestor().getSpec(), isName("foo"));
	}

	@Test
	public void samples() {

		final AscendantsNode result = parse("&foo & bar & baz");
		final AscendantNode[] samples = result.getSamples();

		assertEquals(2, samples.length);

		assertNotNull(result.getAncestor().getSeparator());
		assertThat(result.getAncestor().getSpec(), isName("foo"));

		assertNotNull(samples[0].getSeparator());
		assertThat(samples[0].getSpec(), isName("bar"));

		assertNotNull(samples[1].getSeparator());
		assertThat(samples[1].getSpec(), isName("baz"));
	}

	@Test
	public void ancestorAndSample() {

		final AscendantsNode result = parse("foo & bar");

		assertThat(result, hasRange(0, 9));

		final AscendantNode[] samples = result.getSamples();

		assertEquals(1, samples.length);
		assertThat(result.getAncestor(), hasRange(0, 3));
		assertNull(result.getAncestor().getSeparator());
		assertThat(result.getAncestor().getSpec(), isName("foo"));

		assertThat(samples[0], hasRange(4, 9));
		assertNotNull(samples[0].getSeparator());
		assertThat(samples[0].getSpec(), isName("bar"));
	}

	@Test
	public void ancestorAndSamples() {

		final AscendantsNode result = parse("foo & s1 & s2 & s3");
		final AscendantNode[] samples = result.getSamples();

		assertEquals(3, samples.length);

		assertNull(result.getAncestor().getSeparator());
		assertThat(result.getAncestor().getSpec(), isName("foo"));

		assertNotNull(samples[0].getSeparator());
		assertThat(samples[0].getSpec(), isName("s1"));

		assertNotNull(samples[1].getSeparator());
		assertThat(samples[1].getSpec(), isName("s2"));

		assertNotNull(samples[2].getSeparator());
		assertThat(samples[2].getSpec(), isName("s3"));
	}

	private AscendantsNode parse(String text) {
		return to(
				AscendantsNode.class,
				parse(simpleExpression(), text));
	}

}
