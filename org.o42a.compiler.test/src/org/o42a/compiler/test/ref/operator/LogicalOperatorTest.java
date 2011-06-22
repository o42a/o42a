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
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;


public class LogicalOperatorTest extends CompilerTestCase {

	@Test
	public void isTrue() {
		compile("A := ++1. B := ++false");

		final Obj a = field("a").getArtifact().materialize();
		final Obj b = field("b").getArtifact().materialize();

		assertTrueVoid(a);
		assertFalseVoid(b);
		assertKnownValue(valueOf(b));
	}

	@Test
	public void not() {
		compile("A := --1. B := --false");

		final Obj a = field("a").getArtifact().materialize();
		final Obj b = field("b").getArtifact().materialize();

		assertFalseVoid(a);
		assertKnownValue(valueOf(a));
		assertTrueVoid(b);
	}

	@Test
	public void known() {
		compile(
				"A := +-1.",
				"B := Integer(False ? = 1).",
				"C := +-b.");

		final Obj a = field("a").getArtifact().materialize();
		final Obj b = field("b").getArtifact().materialize();
		final Obj c = field("c").getArtifact().materialize();

		assertTrueVoid(a);
		assertFalseValue(valueOf(b));
		assertUnknownValue(valueOf(b));
		assertFalseVoid(c);
		assertKnownValue(valueOf(c));
	}

	@Test
	public void unknown() {
		compile(
				"A := -+1.",
				"B := Integer(False ? = 1).",
				"C := -+b.");

		final Obj a = field("a").getArtifact().materialize();
		final Obj b = field("b").getArtifact().materialize();
		final Obj c = field("c").getArtifact().materialize();

		assertFalseVoid(a);
		assertKnownValue(valueOf(a));
		assertFalseValue(valueOf(b));
		assertUnknownValue(valueOf(b));
		assertTrueVoid(c);
	}

}
