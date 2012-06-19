/*
    Compiler Tests
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.util.string.Name.caseInsensitiveName;
import static org.o42a.util.string.NameEncoder.NAME_ENCODER;

import org.junit.Test;
import org.o42a.util.string.Name;


public class NameTest {

	@Test
	public void caseInsensitive() {

		final Name name = caseInsensitiveName("Integer");

		assertThat(name.toString(), is("Integer"));
		assertThat(decapitalized(name), is("integer"));
		assertThat(canonical(name), is("integer"));
	}

	@Test
	public void multiWordCaseInsensitive() {

		final Name name = caseInsensitiveName("Some name");

		assertThat(name.toString(), is("Some name"));
		assertThat(decapitalized(name), is("some name"));
		assertThat(canonical(name), is("some name"));
	}

	@Test
	public void abbreviation() {

		final Name name = caseInsensitiveName("URL");

		assertThat(name.toString(), is("URL"));
		assertThat(decapitalized(name), is("URL"));
		assertThat(canonical(name), is("url"));
	}

	@Test
	public void wordAndAbbreviation() {

		final Name name = caseInsensitiveName("Remote URL");

		assertThat(name.toString(), is("Remote URL"));
		assertThat(decapitalized(name), is("remote URL"));
		assertThat(canonical(name), is("remote url"));
	}

	@Test
	public void properNoun() {

		final Name name = caseInsensitiveName("John Smith");

		assertThat(name.toString(), is("John Smith"));
		assertThat(decapitalized(name), is("John Smith"));
		assertThat(canonical(name), is("john smith"));
	}

	@Test
	public void properNounWithConjunction() {

		final Name name = caseInsensitiveName("ACME and Co");

		assertThat(name.toString(), is("ACME and Co"));
		assertThat(decapitalized(name), is("ACME and Co"));
		assertThat(canonical(name), is("acme and co"));
	}

	private static String canonical(Name name) {
		return NAME_ENCODER.canonical().print(name);
	}

	private static String decapitalized(Name name) {
		return NAME_ENCODER.decapitalized().print(name);
	}

}
