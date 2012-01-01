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


public class SubStringTest extends CompilerTestCase {

	@Test
	public void substring() {
		compile("Sub := \"asubc\": substring _ from [1] to [4]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("sub"));
	}

	@Test
	public void shortForm() {
		compile("Sub := \"asubc\": substring [1, 4]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("sub"));
	}

	@Test
	public void leadingSubstring() {
		compile("Sub := \"asubc\": substring _ to [4]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asub"));
	}

	@Test
	public void trailingSubstring() {
		compile("Sub := \"asubc\": substring _ from [1]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("subc"));
	}

	@Test
	public void fullSubstring() {
		compile("Sub := \"asubc\": substring()");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asubc"));
	}

	@Test
	public void invalidRange() {
		expectError("compiler.invalid_substr_range");

		compile("Sub := \"asubc\": substring _ from [4] to [3]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void negativeFrom() {
		expectError("compiler.invalid_substr_from");

		compile("Sub := \"asubc\": substring _ from [-1] to [4]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void invalidTo() {
		expectError("compiler.invalid_substr_to");

		compile("Sub := \"asubc\": substring _ from [1] to [6]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseString() {
		compile(
				"Use namespace 'Test'",
				"Sub := string(False): substring",
				"_ from [rt-integer '1']",
				"_ to [rt-integer '4']");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [1]",
				"_ to [4]");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [integer(False)]",
				"_ to [rt-integer '4']");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\": substring",
				"_ from [rt-integer '1']",
				"_ to [4]");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [rt-integer '1']",
				"_ to [integer(False)]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\": substring",
				"_ from [1]",
				"_ to [rt-integer '4']");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

}
