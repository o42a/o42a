/*
    Parser Tests
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.module;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.module.InclusionNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class InclusionTest extends GrammarTestCase {

	@Test
	public void inclusionWithoutLabel() {
		expectError("missing_inclusion_label");

		final InclusionNode inclusion = parse("***");

		assertRange(0, 3, inclusion);
		assertThat(inclusion.getPrefix().getType().getLength(), is(3));
		assertNull(inclusion.getLabel());
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithoutSuffix() {

		final InclusionNode inclusion = parse("***** Label");

		assertRange(0, 5, inclusion.getPrefix());
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(inclusion.getLabel().getName(), is("label"));
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithSuffix() {

		final InclusionNode inclusion = parse("***** Label ***");

		assertRange(0, 5, inclusion.getPrefix());
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(inclusion.getLabel().getName(), is("label"));
		assertThat(inclusion.getSuffix().getType().getLength(), is(3));
	}

	private InclusionNode parse(String text) {
		return to(
				InclusionNode.class,
				parse(DECLARATIVE.statement(), text));
	}

}
