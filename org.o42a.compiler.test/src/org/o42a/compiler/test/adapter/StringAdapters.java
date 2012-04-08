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
package org.o42a.compiler.test.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class StringAdapters extends CompilerTestCase {

	@Test
	public void intToString() {
		compile(
				"A := 123 456 789",
				"B := string(= A)",
				"C := (`string) a",
				"D := string (= 123)");

		assertThat(
				definiteValue(field("a"), ValueType.INTEGER),
				is(123456789L));
		assertThat(
				definiteValue(field("b"), ValueType.STRING),
				is("123456789"));
		assertThat(
				definiteValue(linkTarget(field("c")), ValueType.STRING),
				is("123456789"));
		assertThat(
				definiteValue(field("d"), ValueType.STRING),
				is("123"));
	}

	@Test
	public void floatToString() {
		compile(
				"A := float '123 456,789'",
				"B := string(= A)",
				"C := (`string) a",
				"D := string (= float '123.456')");

		assertThat(
				definiteValue(field("a"), ValueType.FLOAT),
				is(123456.789));
		assertThat(
				definiteValue(field("b"), ValueType.STRING),
				is("123456.789"));
		assertThat(
				definiteValue(linkTarget(field("c")), ValueType.STRING),
				is("123456.789"));
		assertThat(
				definiteValue(field("d"), ValueType.STRING),
				is("123.456"));
	}

}
