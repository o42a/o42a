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
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class DecimalTest extends GrammarTestCase {

	@Test
	public void simplyDecimal() {

		final DecimalNode result = parse("12345");

		assertThat(result.getNumber(), is("12345"));
	}

	@Test
	public void spaceSeparated() {

		final DecimalNode result = parse("1 234 567");

		assertThat(result.getNumber(), is("1234567"));
	}

	@Test
	public void tooManySpaces() {
		expectError("invalid_space_in_number");
		expectError("invalid_space_in_number");

		final DecimalNode result = parse("1  234  567    ");

		assertThat(result.getNumber(), is("1234567"));
	}

	@Test
	public void whitespace() {
		expectError("invalid_space_in_number");

		final DecimalNode result = parse("1 234\n 567    ");

		assertThat(result.getNumber(), is("1234567"));
	}

	private DecimalNode parse(String text) {
		return parse(Grammar.decimal(), text);
	}

}
