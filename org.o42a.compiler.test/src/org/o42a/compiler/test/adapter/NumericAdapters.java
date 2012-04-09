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


public class NumericAdapters extends CompilerTestCase {

	@Test
	public void intToFloat() {
		compile(
				"A := float(= 456)",
				"B := 7890",
				"C := float(= B)",
				"D := (`float) b");

		assertThat(definiteValue(field("a"), ValueType.FLOAT), is(456.0));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(7890L));
		assertThat(definiteValue(field("c"), ValueType.FLOAT), is(7890.0));
		assertThat(
				definiteValue(linkTarget(field("d")), ValueType.FLOAT),
				is(7890.0));
	}

	@Test
	public void operators() {
		compile(
				"A := float '0.12' + 42",
				"B := float '0.1' * 42",
				"C := float '9' / 2",
				"D := float '12.12' - 2");

		assertThat(definiteValue(field("a"), ValueType.FLOAT), is(42.12));
		assertThat(definiteValue(field("b"), ValueType.FLOAT), is(4.2));
		assertThat(definiteValue(field("c"), ValueType.FLOAT), is(4.5));
		assertThat(definiteValue(field("d"), ValueType.FLOAT), is(10.12));
	}

	@Test
	public void comparison() {
		compile(
				"A := float '0.12' < 42",
				"B := float '0.1' >= 42",
				"C := float '9' == 9",
				"D := float '12.12' <> 12");

		assertTrueVoid(field("a"));
		assertFalseVoid(field("b"));
		assertTrueVoid(field("c"));
		assertTrueVoid(field("d"));
	}

}
