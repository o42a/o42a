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
package org.o42a.compiler.test.field;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class PrivateFieldNonVisibilityTest extends CompilerTestCase {

	@Test
	public void inAnotherSource() {
		expectError("compiler.undefined_member");

		addSource(
				"a",
				"A := void",
				"=========",
				":Foo := 36");
		compile("B := a: foo");
	}

	@Test
	public void deepInAnotherSource() {
		expectError("compiler.undefined_member");

		addSource(
				"a",
				"A := void",
				"=========",
				"C := b: foo");
		addSource(
				"a/b",
				"B := void",
				"=========");
		addSource(
				"a/b/foo",
				":Foo := 64",
				"=========");
		compile("");
	}

	@Test
	public void derive() {
		expectError("compiler.unresolved");

		compile(
				"A := void (",
				"  :Foo := 1",
				")",
				"B := a (",
				"  Bar := foo",
				")");
	}

	@Test
	public void override() {
		expectError("compiler.cant_override_unknown");

		compile(
				"A := void (",
				"  :Foo := 1",
				")",
				"B := a (",
				"  foo = 2",
				")");
	}

}
