/*
    Parser Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.file;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.file.InclusionNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class InclusionTest extends GrammarTestCase {

	@Test
	public void inclusionWithoutLabel() {
		expectError("missing_inclusion_tag");

		final InclusionNode inclusion = parse("***");

		assertThat(inclusion, hasRange(0, 3));
		assertThat(inclusion.getPrefix().getType().getLength(), is(3));
		assertNull(inclusion.getTag());
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithoutSuffix() {

		final InclusionNode inclusion = parse("***** Label");

		assertThat(inclusion.getPrefix(), hasRange(0, 5));
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(canonicalName(inclusion.getTag()), is("label"));
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithSuffix() {

		final InclusionNode inclusion = parse("***** Label **");

		assertThat(inclusion.getPrefix(), hasRange(0, 5));
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(canonicalName(inclusion.getTag()), is("label"));
		assertThat(inclusion.getSuffix().getType().getLength(), is(2));
	}

	private InclusionNode parse(String text) {
		return to(
				InclusionNode.class,
				parse(DECLARATIVE.statement(), text));
	}

}
