/*
    Compiler Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.test.operator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.value.ValueType;


public class ComparisonOperatorTest extends CompilerTestCase {

	@Test
	public void equals() {
		compile(
				"A := void(@Operators: equals :=> void).",
				"B := A == 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertTrueValue(b.getValue());
	}

	@Test
	public void notEquals() {
		compile(
				"A := void(@Operators: equals :=> void()).",
				"B := A <> 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertFalseValue(b.getValue());
	}

	@Test
	public void less() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A < 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertTrueValue(b.getValue());
	}

	@Test
	public void lessOrEquals() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A <= 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertTrueValue(b.getValue());
	}

	@Test
	public void greater() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A > 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertFalseValue(b.getValue());
	}

	@Test
	public void greaterOrEquals() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A >= 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertFalseValue(b.getValue());
	}

	@Test
	public void compareEquals() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A == 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertFalseValue(b.getValue());
	}

	@Test
	public void compareNotEquals() {
		compile(
				"A := void(@Operators: compare :=> -1).",
				"B := A <> 2");

		final Obj b = getField("b").getArtifact().materialize();

		assertEquals(ValueType.VOID, b.getValueType());
		assertTrueValue(b.getValue());
	}

}
