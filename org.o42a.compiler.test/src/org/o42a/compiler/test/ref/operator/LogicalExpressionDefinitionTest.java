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
import org.o42a.util.Source;


public class LogicalExpressionDefinitionTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Test
	public void isTrue() {
		compile("A := void. B := void(= ++A). C := b.");

		assertTrueVoid(this.a);
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Test
	public void notTrue() {
		compile("A := void. B := void(= --A). C := b.");

		assertTrueVoid(this.a);
		assertFalseVoid(this.b);
		assertFalseVoid(this.c);
	}

	@Test
	public void notFalse() {
		compile("A := false. B := void(= --A). C := b.");

		assertFalseVoid(this.a);
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Test
	public void known() {
		compile(
				"A := false.",
				"B := void(= +-A).",
				"C := b.");

		assertKnownValue(valueOf(this.a));
		assertFalseVoid(this.a);
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Test
	public void notKnown() {
		compile(
				"A := void(False? = Void).",
				"B := void(= +-A).",
				"C := b.");

		assertUnknownValue(valueOf(this.a));
		assertFalseVoid(this.b);
		assertFalseVoid(this.c);
	}

	@Test
	public void notUnknown() {
		compile(
				"A := void.",
				"B := void(= -+A).",
				"C := b.");

		assertKnownValue(valueOf(this.a));
		assertTrueVoid(this.a);
		assertFalseVoid(this.b);
		assertFalseVoid(this.c);
	}

	@Test
	public void unknown() {
		compile(
				"A := void(False? = Void).",
				"B := void(= -+A).",
				"C := b.");

		assertUnknownValue(valueOf(this.a));
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.a = field("a").getArtifact().materialize();
		this.b = field("b").getArtifact().materialize();
		this.c = field("c").getArtifact().materialize();
	}

}
