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
package org.o42a.compiler.test.ref.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;


public class BinaryOperatorTest extends CompilerTestCase {

	@Test
	public void add() {
		compile(
				"A := void(@Operators: add :=> 1).",
				"B := a + 2");

		final Obj b = field("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void subtract() {
		compile(
				"A := void(@Operators: subtract :=> 1).",
				"B := a - 2");

		final Obj b = field("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void multiply() {
		compile(
				"A := void(@Operators: multiply :=> 1).",
				"B := A * 2");

		final Obj b = field("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void divide() {
		compile(
				"A := void(@Operators: divide :=> 1).",
				"B := A / 2");

		final Obj b = field("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

}
