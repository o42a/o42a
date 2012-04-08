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

		assertRange(0, 4, result);
		assertFalse(result.hasSamples());
		assertNotNull(result.getAncestor().getSeparator());
		assertRange(0, 4, result.getAncestor());
		assertName("foo", result.getAncestor().getSpec());
	}

	@Test
	public void samples() {

		final AscendantsNode result = parse("&foo & bar & baz");
		final AscendantNode[] samples = result.getSamples();

		assertEquals(2, samples.length);

		assertNotNull(result.getAncestor().getSeparator());
		assertName("foo", result.getAncestor().getSpec());

		assertNotNull(samples[0].getSeparator());
		assertName("bar", samples[0].getSpec());

		assertNotNull(samples[1].getSeparator());
		assertName("baz", samples[1].getSpec());
	}

	@Test
	public void ancestorAndSample() {

		final AscendantsNode result = parse("foo & bar");

		assertRange(0, 9, result);

		final AscendantNode[] samples = result.getSamples();

		assertEquals(1, samples.length);
		assertRange(0, 3, result.getAncestor());
		assertNull(result.getAncestor().getSeparator());
		assertName("foo", result.getAncestor().getSpec());

		assertRange(4, 9, samples[0]);
		assertNotNull(samples[0].getSeparator());
		assertName("bar", samples[0].getSpec());
	}

	@Test
	public void ancestorAndSamples() {

		final AscendantsNode result = parse("foo & s1 & s2 & s3");
		final AscendantNode[] samples = result.getSamples();

		assertEquals(3, samples.length);

		assertNull(result.getAncestor().getSeparator());
		assertName("foo", result.getAncestor().getSpec());

		assertNotNull(samples[0].getSeparator());
		assertName("s1", samples[0].getSpec());

		assertNotNull(samples[1].getSeparator());
		assertName("s2", samples[1].getSpec());

		assertNotNull(samples[2].getSeparator());
		assertName("s3", samples[2].getSpec());
	}

	private AscendantsNode parse(String text) {
		return to(
				AscendantsNode.class,
				parse(simpleExpression(), text));
	}

}
