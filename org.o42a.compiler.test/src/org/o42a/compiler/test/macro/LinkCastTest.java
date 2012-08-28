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
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LinkCastTest extends CompilerTestCase {

	@Test
	public void linkToValue() {
		compile(
				"A := `123",
				"B := string(= A #cast)");

		assertThat(definiteValue(field("b"), ValueType.STRING), is("123"));
	}

	@Test
	public void linkToLink() {
		compile(
				"A := `123",
				"B := (`string) a# cast");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
	}

	@Test
	public void propagateCast() {
		compile(
				"To string :=> string (",
				"  #T :=< void",
				"  Arg :=< (`#t) void",
				"  = Arg #cast",
				")",
				"A := to string(T = integer. Arg = 456)");

		assertThat(definiteValue(field("a"), ValueType.STRING), is("456"));
	}

}
