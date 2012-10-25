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
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.ValueKnowledge.RUNTIME_VALUE;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.array.Array;


public class ArrayAsRowTest extends CompilerTestCase {

	@Test
	public void arrayAsRow() {
		compile(
				"A := array (`integer) [[1, 2, 3]]",
				"B := a: as row");

		assertThat(valueOf(field("b")).getKnowledge(), is(RUNTIME_VALUE));
	}

	@Test
	public void emptyArrayAsRow() {
		compile(
				"A := array (`integer) [[]]",
				"B := a: as row");

		final Array array = definiteValue(field("b"));

		assertThat(array.length(), is(0));
	}

	@Test
	public void falseArrayAsRow() {
		compile(
				"A := array (`integer) (False, = [1, 2, 3])",
				"B := a: as row");

		assertFalseValue(valueOf(field("b")));
	}

}
