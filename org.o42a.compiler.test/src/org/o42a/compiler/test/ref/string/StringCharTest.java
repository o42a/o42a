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
package org.o42a.compiler.test.ref.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class StringCharTest extends CompilerTestCase {

	@Test
	public void firstChar() {
		compile("Chr := \"abc\"[0]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("a"));
	}

	@Test
	public void lastChar() {
		compile("Chr := \"abc\"[2]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("c"));
	}

	@Test
	public void someChar() {
		compile("Chr := \"abc\"[1]");

		assertThat(definiteValue(field("chr"), ValueType.STRING), is("b"));
	}

	@Test
	public void negativeCharIndex() {
		expectError("compiler.invalid_char_index");

		compile("Chr := \"abc\"[-1]");

		assertFalseValue(valueOf(field("chr"), ValueType.STRING));
	}

	@Test
	public void invalidCharIndex() {
		expectError("compiler.invalid_char_index");

		compile("Chr := \"abc\"[3]");

		assertFalseValue(valueOf(field("chr"), ValueType.STRING));
	}

	@Test
	public void falseString() {
		compile(
				"Str := string(False)",
				"Chr := str[0]");

		assertFalseValue(valueOf(field("chr"), ValueType.STRING));
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Chr := rt-string 'abc' [0]");

		assertRuntimeValue(valueOf(field("chr"), ValueType.STRING));
	}

	@Test
	public void falseIndex() {
		compile("Chr := \"abc\"[integer(False)]");

		assertFalseValue(valueOf(field("chr"), ValueType.STRING));
	}

	@Test
	public void runtimeIndex() {
		compile(
				"Use namespace 'Test'",
				"Chr := \"abc\"[rt-integer '1']");

		assertRuntimeValue(valueOf(field("chr"), ValueType.STRING));
	}

}
