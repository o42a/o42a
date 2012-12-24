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


public class BinaryInheritanceTest extends CompilerTestCase {

	private Field aResult;
	private Field bResult;
	private Field cResult;
	private Field dResult;

	@Test
	public void add() {
		compile(
				"A := void(",
				"  F := 2.",
				"  G := 3.",
				"  Result := f + g.",
				").",
				"B := a(F = 4. G = -7).",
				"C := b.",
				"D := b()");

		assertThat(definiteValue(this.aResult, ValueType.INTEGER), is(5L));
		assertThat(definiteValue(this.bResult, ValueType.INTEGER), is(-3L));
		assertThat(definiteValue(this.cResult, ValueType.INTEGER), is(-3L));
		assertThat(definiteValue(this.dResult, ValueType.INTEGER), is(-3L));
	}

	@Test
	public void subtract() {
		compile(
				"A := void(",
				"  F := 2.",
				"  G := 3.",
				"  Result := f - g.",
				").",
				"B := a(F = 4. G = -7).",
				"C := b.",
				"D := b()");

		assertThat(definiteValue(this.aResult, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.bResult, ValueType.INTEGER), is(11L));
		assertThat(definiteValue(this.cResult, ValueType.INTEGER), is(11L));
		assertThat(definiteValue(this.dResult, ValueType.INTEGER), is(11L));
	}

	@Test
	public void multiply() {
		compile(
				"A := void(",
				"  F := 2.",
				"  G := 3.",
				"  Result := f * g.",
				").",
				"B := a(F = 4. G = -7).",
				"C := b.",
				"D := b()");

		assertThat(definiteValue(this.aResult, ValueType.INTEGER), is(6L));
		assertThat(definiteValue(this.bResult, ValueType.INTEGER), is(-28L));
		assertThat(definiteValue(this.cResult, ValueType.INTEGER), is(-28L));
		assertThat(definiteValue(this.dResult, ValueType.INTEGER), is(-28L));
	}

	@Test
	public void divide() {
		compile(
				"A := void(",
				"  F := 3.",
				"  G := 2.",
				"  Result := f / g.",
				").",
				"B := a(F = -74. G = 7).",
				"C := b.",
				"D := b()");

		assertThat(definiteValue(this.aResult, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.bResult, ValueType.INTEGER), is(-10L));
		assertThat(definiteValue(this.cResult, ValueType.INTEGER), is(-10L));
		assertThat(definiteValue(this.dResult, ValueType.INTEGER), is(-10L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aResult = field("a", "result");
		this.bResult = field("b", "result");
		this.cResult = field("c", "result");
		this.dResult = field("c", "result");
	}
}
