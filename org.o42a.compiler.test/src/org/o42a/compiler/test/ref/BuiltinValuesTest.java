/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class BuiltinValuesTest extends CompilerTestCase {

	@Test
	public void voidValueType() {
		compile("Void value := void");

		final Obj field = field("void value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.VOID));
	}

	@Test
	public void integerValue() {
		compile("Integer value := 12345678900");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void positiveIntegerValue() {
		compile("Integer value := +12345678900");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void negativeIntegerValue() {
		compile("Integer value := -12345678900");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(-12345678900L));
	}

	@Test
	public void integerByStringValue() {
		compile("Integer value := integer '12345678900'");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void positiveIntegerByStringValue() {
		compile("Integer value := integer '+12345678900'");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(12345678900L));
	}

	@Test
	public void negativeIntegerByStringValue() {
		compile("Integer value := integer '-12345678900'");

		final Obj field = field("integer value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(definiteValue(field, ValueType.INTEGER), is(-12345678900L));
	}

	@Test
	public void floatValue() {
		compile("Float value := 1234567890.25");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void positiveFloatValue() {
		compile("Float value := +1234567890,25");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void negativeFloatValue() {
		compile("Float value := -123,25e10");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(-123.25e10d));
	}

	@Test
	public void floatValueByString() {
		compile("Float value := float '1234567890.25'");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void positiveFloatValueByString() {
		compile("Float value := float '+1234567890.25'");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(1234567890.25d));
	}

	@Test
	public void negativeFloatValueByString() {
		compile("Float value := float '-1234567890.25'");

		final Obj field = field("float value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.FLOAT));
		assertThat(definiteValue(field, ValueType.FLOAT), is(-1234567890.25d));
	}

	@Test
	public void stringValue() {
		compile("String value := \"abc\"");

		final Obj field = field("string value").toObject();

		assertThat(field.type().getValueType(), valueType(ValueType.STRING));
		assertThat(definiteValue(field, ValueType.STRING), is("abc"));
	}

}
