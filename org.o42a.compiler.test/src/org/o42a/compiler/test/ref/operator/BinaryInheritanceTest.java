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
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;
import org.o42a.util.Source;


public class BinaryInheritanceTest extends CompilerTestCase {

	private Field<?> aResult;
	private Field<?> bResult;
	private Field<?> cResult;
	private Field<?> dResult;

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
	protected void compile(Source source) {
		super.compile(source);

		this.aResult = field(field("a"), "result");
		this.bResult = field(field("b"), "result");
		this.cResult = field(field("c"), "result");
		this.dResult = field(field("c"), "result");
	}
}
