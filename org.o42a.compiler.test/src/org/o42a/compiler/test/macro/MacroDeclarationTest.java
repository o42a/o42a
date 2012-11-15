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


public class MacroDeclarationTest extends CompilerTestCase {

	@Test
	public void macroDeclaration() {
		compile(
				"A := 123",
				"#B := a + 1");

		final Obj b = field("b").toObject();

		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroObject() {
		compile(
				"A := 123",
				"B := macro(= A + 1)");

		final Obj b = field("b").toObject();

		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroDeclarationByMacroValue() {
		compile(
				"#A := 123",
				"#B := a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(a.type().getValueType().isMacro(), is(true));
		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroObjectByMacroValue() {
		compile(
				"#A := 123",
				"B := macro(= A)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(a.type().getValueType().isMacro(), is(true));
		assertThat(b.type().getValueType().isMacro(), is(true));
	}

}
