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
package org.o42a.compiler.test.declarative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ConditionTest extends CompilerTestCase {

	@Test
	public void condition() {
		compile(
				"A := void (",
				"  Condition := void",
				"  Value := void (Condition)",
				")",
				"B := a (Condition = false)",
				"C := b");

		assertTrueVoid(field("a", "value"));
		assertFalseVoid(field("b", "value"));
		assertFalseVoid(field("c", "value"));
	}

	@Test
	public void not() {
		compile(
				"A := void (",
				"  Condition := (`void) false",
				"  Value := void (--Condition)",
				")",
				"B := a (Condition = void)",
				"C := b");

		assertTrueVoid(field("a", "value"));
		assertFalseVoid(field("b", "value"));
		assertFalseVoid(field("c", "value"));
	}

	@Test
	public void issue() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = Void. = False",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertFalseVoid(field("c"));
	}

	@Test
	public void unlessIssue() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = False. = Void",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertFalseVoid(field("a"));
		assertTrueVoid(field("b"));
		assertTrueVoid(field("c"));
	}

}
