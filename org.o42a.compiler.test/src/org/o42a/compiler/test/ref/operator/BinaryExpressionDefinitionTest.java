/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class BinaryExpressionDefinitionTest extends CompilerTestCase {

	private Field c;
	private Field d;
	private Field e;

	@Test
	public void add() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := integer(= A + b).",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(5L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(5L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(5L));
	}

	@Test
	public void subtract() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := integer(= A - b).",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(-1L));
	}

	@Test
	public void multiply() {
		compile(
				"A := 2.",
				"B := 3.",
				"C := integer(= A * b).",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(6L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(6L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(6L));
	}

	@Test
	public void divide() {
		compile(
				"A := 14.",
				"B := 3.",
				"C := integer(= A / b).",
				"D := c.",
				"E := c().");

		assertThat(definiteValue(this.c, ValueType.INTEGER), is(4L));
		assertThat(definiteValue(this.d, ValueType.INTEGER), is(4L));
		assertThat(definiteValue(this.e, ValueType.INTEGER), is(4L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.c = field("c");
		this.d = field("d");
		this.e = field("e");
	}

}
