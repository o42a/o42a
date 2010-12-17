/*
    Compiler Tests
    Copyright (C) 2010 Ruslan Lopatin

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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;


public class UnaryOperatorTest extends CompilerTestCase {

	@Test
	public void plus() {
		compile(
				"A := void(@$$Operators: plus :=> 1).",
				"B := +a");

		final Obj b = getField("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

	@Test
	public void minus() {
		compile(
				"A := void(@Operators: minus :=> 1).",
				"B := -a");

		final Obj b = getField("b").getArtifact().materialize();

		assertTrue(b.derivedFrom(this.context.getIntrinsics().getInteger()));
		assertEquals(1L, definiteValue(b));
	}

}
