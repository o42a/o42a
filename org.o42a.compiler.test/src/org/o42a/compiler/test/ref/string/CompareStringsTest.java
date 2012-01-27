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
package org.o42a.compiler.test.ref.string;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class CompareStringsTest extends CompilerTestCase {

	@Test
	public void equal() {
		compile(
				"Res1 := \"abc\" == \"abc\"",
				"Res2 := \"abc\" == \"a\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
	}

	@Test
	public void notEqual() {
		compile(
				"Res1 := \"abc\" <> \"a\"",
				"Res2 := \"abc\" <> \"abc\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
	}

	@Test
	public void less() {
		compile(
				"Res1 := \"a\" < \"b\"",
				"Res2 := \"a\" < \"a\"",
				"Res3 := \"b\" < \"a\"");

		assertTrueVoid(field("res1"));
		assertFalseVoid(field("res2"));
		assertFalseVoid(field("res3"));
	}

	@Test
	public void lessOrEqual() {
		compile(
				"Res1 := \"a\" <= \"b\"",
				"Res2 := \"a\" <= \"a\"",
				"Res3 := \"b\" <= \"a\"");

		assertTrueVoid(field("res1"));
		assertTrueVoid(field("res2"));
		assertFalseVoid(field("res3"));
	}

	@Test
	public void greater() {
		compile(
				"Res1 := \"a\" > \"b\"",
				"Res2 := \"a\" > \"a\"",
				"Res3 := \"b\" > \"a\"");

		assertFalseVoid(field("res1"));
		assertFalseVoid(field("res2"));
		assertTrueVoid(field("res3"));
	}

	@Test
	public void greaterOrEqual() {
		compile(
				"Res1 := \"a\" >= \"b\"",
				"Res2 := \"a\" >= \"a\"",
				"Res3 := \"b\" >= \"a\"");

		assertFalseVoid(field("res1"));
		assertTrueVoid(field("res2"));
		assertTrueVoid(field("res3"));
	}

}
