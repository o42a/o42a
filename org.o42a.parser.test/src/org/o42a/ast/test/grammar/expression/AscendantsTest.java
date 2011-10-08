/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.AscendantNode;
import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class AscendantsTest extends GrammarTestCase {

	@Test
	public void sample() {

		final AscendantsNode result = parse("&foo");

		assertRange(0, 4, result);

		final AscendantNode[] ascendants = result.getAscendants();

		assertEquals(1, ascendants.length);

		assertNotNull(ascendants[0].getSeparator());
		assertRange(0, 4, ascendants[0]);
		assertName("foo", ascendants[0].getSpec());
	}

	@Test
	public void samples() {

		final AscendantsNode result = parse("&foo & bar & baz");
		final AscendantNode[] ascendants = result.getAscendants();

		assertEquals(3, ascendants.length);

		assertNotNull(ascendants[0].getSeparator());
		assertName("foo", ascendants[0].getSpec());

		assertNotNull(ascendants[1].getSeparator());
		assertName("bar", ascendants[1].getSpec());

		assertNotNull(ascendants[2].getSeparator());
		assertName("baz", ascendants[2].getSpec());
	}

	@Test
	public void ancestorAndSample() {

		final AscendantsNode result = parse("foo & bar");

		assertRange(0, 9, result);

		final AscendantNode[] ascendants = result.getAscendants();

		assertEquals(2, ascendants.length);
		assertRange(0, 3, ascendants[0]);
		assertNull(ascendants[0].getSeparator());
		assertName("foo", ascendants[0].getSpec());

		assertRange(4, 9, ascendants[1]);
		assertNotNull(ascendants[1].getSeparator());
		assertName("bar", ascendants[1].getSpec());
	}

	@Test
	public void ancestorAndSamples() {

		final AscendantsNode result = parse("foo & s1 & s2 & s3");
		final AscendantNode[] ascendants = result.getAscendants();

		assertEquals(4, ascendants.length);

		assertNull(ascendants[0].getSeparator());
		assertName("foo", ascendants[0].getSpec());

		assertNotNull(ascendants[1].getSeparator());
		assertName("s1", ascendants[1].getSpec());

		assertNotNull(ascendants[2].getSeparator());
		assertName("s2", ascendants[2].getSpec());

		assertNotNull(ascendants[3].getSeparator());
		assertName("s3", ascendants[3].getSpec());
	}

	@Test
	public void arrayType() {

		final AscendantsNode result = parse("foo & [bar] & baz");
		final AscendantNode[] ascendants = result.getAscendants();

		assertEquals(3, ascendants.length);

		assertNull(ascendants[0].getSeparator());
		assertName("foo", ascendants[0].getSpec());

		assertNotNull(ascendants[1].getSeparator());

		final ArrayTypeNode arrayType =
				to(ArrayTypeNode.class, ascendants[1].getSpec());

		assertNull(arrayType.getAncestor());
		assertNotNull(arrayType.getOpening());
		assertName("bar", arrayType.getItemType());
		assertNotNull(arrayType.getClosing());

		assertNotNull(ascendants[2].getSeparator());
		assertName("baz", ascendants[2].getSpec());
	}

	private AscendantsNode parse(String text) {
		return to(
				AscendantsNode.class,
				parse(DECLARATIVE.simpleExpression(), text));
	}

}
