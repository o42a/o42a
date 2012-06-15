/*
    Compiler Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class BuiltinValuesTest extends CompilerTestCase {

	@Test
	public void voidValue() {
		compile("Void value := void");

		final Obj field = field("void value").toObject();

		assertEquals(ValueType.VOID, field.value().getValueType());
	}

	@Test
	public void integerValue() {
		compile("Integer value := 12345678900");

		final Obj field = field("integer value").toObject();

		assertEquals(ValueType.INTEGER, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void integerByStringValue() {
		compile("Integer value := integer '12345678900'");

		final Obj field = field("integer value").toObject();

		assertEquals(ValueType.INTEGER, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void positiveIntegerByStringValue() {
		compile("Integer value := integer '+12345678900'");

		final Obj field = field("integer value").toObject();

		assertEquals(ValueType.INTEGER, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void negativeIntegerByStringValue() {
		compile("Integer value := integer '-12345678900'");

		final Obj field = field("integer value").toObject();

		assertEquals(ValueType.INTEGER, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.INTEGER), is(-12345678900L));
	}

	@Test
	public void floatValue() {
		compile("Float value := float '1234567890.25'");

		final Obj field = field("float value").toObject();

		assertEquals(ValueType.FLOAT, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void positiveFloatValue() {
		compile("Float value := float '+1234567890.25'");

		final Obj field = field("float value").toObject();

		assertEquals(ValueType.FLOAT, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void negativeFloatValue() {
		compile("Float value := float '-1234567890.25'");

		final Obj field = field("float value").toObject();

		assertEquals(ValueType.FLOAT, field.value().getValueType());
		assertThat(definiteValue(field, ValueType.FLOAT), is(-1234567890.25d));
	}

	@Test
	public void stringValue() {
		compile("String value := \"abc\"");

		final Obj field = field("string value").toObject();

		assertEquals(ValueType.STRING, field.value().getValueType());
		assertEquals("abc", definiteValue(field));
	}

}
