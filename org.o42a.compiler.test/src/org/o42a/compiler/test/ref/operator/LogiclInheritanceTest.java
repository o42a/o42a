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
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class LogiclInheritanceTest extends CompilerTestCase {

	private Obj aBar;
	private Obj bBar;
	private Obj cBar;

	@Test
	public void isTrue() {
		compile(
				"A := void(",
				"  Foo := void",
				"  Bar := ++foo",
				")",
				"B := a(Foo = false)",
				"C := b");

		assertTrueVoid(this.aBar);
		assertFalseVoid(this.bBar);
		assertFalseVoid(this.cBar);
	}

	@Test
	public void not() {
		compile(
				"A := void(",
				"  Foo := void",
				"  Bar := --foo",
				")",
				"B := a(Foo = false)",
				"C := b");

		assertFalseVoid(this.aBar);
		assertTrueVoid(this.bBar);
		assertTrueVoid(this.cBar);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aBar = field("a", "bar").toObject();
		this.bBar = field("b", "bar").toObject();
		this.cBar = field("c", "bar").toObject();
	}

}
