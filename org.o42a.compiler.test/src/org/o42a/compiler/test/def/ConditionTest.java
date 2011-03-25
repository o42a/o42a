/*
    Compiler Tests
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.test.def;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ConditionTest extends CompilerTestCase {

	@Test
	public void condition() {
		compile(
				"A := void(",
				"  Condition := void.",
				"  Value := void(Condition).",
				").",
				"B := A(Condition = false).",
				"C := B.");

		assertTrueVoid(field(field("a"), "value").getArtifact().materialize());
		assertFalseVoid(field(field("b"), "value").getArtifact().materialize());
		assertFalseVoid(field(field("c"), "value").getArtifact().materialize());
	}

	@Test
	public void not() {
		compile(
				"A := void(",
				"  Condition := (`void) false.",
				"  Value := void(--Condition).",
				").",
				"B := A(Condition = void).",
				"C := B.");

		assertTrueVoid(field(field("a"), "value").getArtifact().materialize());
		assertFalseVoid(field(field("b"), "value").getArtifact().materialize());
		assertFalseVoid(field(field("c"), "value").getArtifact().materialize());
	}

}
