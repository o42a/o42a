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
package org.o42a.compiler.test.def;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class DefinitionBlockTest extends CompilerTestCase {

	@Test
	public void conditionAfterField() {
		compile(
				"(",
				"  A := 42.",
				"  False.",
				").");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(42L));
		assertFalseVoid(this.module);
	}

	@Test
	public void valueAfterField() {
		compile(
				"A := string(",
				"  (",
				"    Foo := 42.",
				"    = \"value\".",
				"  ).",
				").");

		assertThat(
				definiteValue(field("a", "foo"), ValueType.INTEGER),
				is(42L));
		assertThat(
				definiteValue(field("a"), ValueType.STRING),
				is("value"));
	}

	@Test
	public void fieldValueAndCondition() {
		compile(
				"A := string(",
				"  (",
				"    Foo := 42.",
				"    = \"value\".",
				"    False.",
				"  ).",
				").");

		assertThat(
				definiteValue(field("a", "foo"), ValueType.INTEGER),
				is(42L));
		assertFalseValue(valueOf(field("a")));
	}

	@Test
	public void fieldConditionField() {
		compile(
				"(",
				"  A := 42.",
				"  False.",
				"  B := 34.",
				").");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(42L));
		assertFalseVoid(this.module);
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(34L));
	}
}
