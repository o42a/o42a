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
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class MacroExpansionTest extends CompilerTestCase {

	@Test
	public void linkInterface() {
		compile(
				"#A := integer",
				"B := (`#a) 123");

		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(123L));
	}

	@Test
	public void linkObject() {
		compile(
				"#A := integer",
				"B := link (`#a) 123");

		final Obj b = field("b").toObject();

		assertThat(definiteValue(linkTarget(b), ValueType.INTEGER), is(123L));
	}

	@Test
	public void trueCondition() {
		compile(
				"#A := 5",
				"B := void (#A)");

		assertTrueVoid(field("b"));
	}

	@Test
	public void falseCondition() {
		compile(
				"#A := false",
				"B := void (#A)");

		assertFalseVoid(field("b"));
	}

	@Test
	public void selfAssignment() {
		compile(
				"#A := 5",
				"B := integer (= #A)");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(5L));
	}

	@Test
	public void selfAssignmentWithAdapter() {
		compile(
				"#A := 5",
				"B := float (= #A)");

		assertThat(definiteValue(field("b"), ValueType.FLOAT), is(5.0));
	}

}
