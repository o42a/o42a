/*
    Compiler Tests
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
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ComplexPhraseTest extends CompilerTestCase {

	@Test
	public void phraseAsPhrasePrefix() {
		compile("A := (integer '5') '7'");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(7L));
	}

	@Test
	public void phraseAsPhrasePrefix2() {
		compile(
				"Str :=> string(",
				"  <*''> ()",
				")",
				"A := (str 'abc')[1]");

		assertThat(definiteValue(field("a"), ValueType.STRING), is("b"));
	}

}
