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
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class KeepValueTest extends CompilerTestCase {

	@Test
	public void linkByValue() {
		compile(
				"A := 5",
				"B := `//a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void linkByAdapterValue() {
		compile(
				"A := 5",
				"B := (`string) //a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("5"));
	}

	@Test
	public void localFieldDefinition() {
		compile(
				"A := 5",
				"B := integer ({",
				"  L := //a",
				"  = L",
				"})");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(5L));
	}

}
