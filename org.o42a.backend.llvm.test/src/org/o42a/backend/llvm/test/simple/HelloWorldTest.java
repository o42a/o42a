/*
    Compiler LLVM Back-end Tests
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
package org.o42a.backend.llvm.test.simple;

import org.junit.Test;
import org.o42a.backend.llvm.test.GeneratorTestCase;


public class HelloWorldTest extends GeneratorTestCase {

	@Test
	public void helloWorld() {
		setModuleName("hello_world");
		compile(
				"Use namespace 'Console'.",
				"@Main :=> *{",
				"  Print \"Hello, World!\" nl.",
				"}.");

	}

}
