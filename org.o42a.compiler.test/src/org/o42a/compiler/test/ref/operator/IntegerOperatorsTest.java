/*
    Compiler Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.util.Source;


public class IntegerOperatorsTest extends CompilerTestCase {

	private Obj result;

	@Test
	public void plus() {
		compile("Result := +2");

		assertEquals(2L, definiteValue(this.result));
	}

	@Test
	public void minus() {
		compile("Result := -2");

		assertEquals(-2L, definiteValue(this.result));
	}

	@Test
	public void add() {
		compile("Result := 2 + 3");

		assertEquals(5L, definiteValue(this.result));
	}

	@Test
	public void subtract() {
		compile("Result := 2 - 3");

		assertEquals(-1L, definiteValue(this.result));
	}

	@Test
	public void multiply() {
		compile("Result := 2 * 3");

		assertEquals(6L, definiteValue(this.result));
	}

	@Test
	public void divide() {
		compile("Result := 6 / 2");

		assertEquals(3L, definiteValue(this.result));
	}

	@Test
	public void divideByZero() {
		expectError("compiler.arithmetic_error");

		compile("Result := 1 / 0");

		assertEquals(ValueType.INTEGER, this.result.toObject().getValueType());
		assertFalseValue(
				this.result.toObject().value().useBy(USE_CASE).getValue());
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.result = field("result").getArtifact().materialize();
		assertEquals(ValueType.INTEGER, this.result.getValueType());
		assertTrue(this.result.type().useBy(USE_CASE).inherits(
				this.context.getIntrinsics().getInteger()
				.type().useBy(USE_CASE)));
	}

}
