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
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeAltErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer({(...); void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedRepeat() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer(Foo: {... foo; void})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer({(!); void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedExit() {
		expectError("compiler.unreachable_alternative");
		compile("A := integer(Foo: {(... foo!); void})");
	}

}
