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


public strictfp class FloatOperatorsTest extends CompilerTestCase {

	private Obj result;

	@Test
	public void plus() {
		compile("Result := +float '123.45'");

		assertEquals(123.45, definiteValue(this.result));
	}

	@Test
	public void minus() {
		compile("Result := -float '123.45'");

		assertEquals(-123.45, definiteValue(this.result));
	}

	@Test
	public void add() {
		compile("Result := float '1.2' + float '3.4'");

		assertEquals(1.2 + 3.4, definiteValue(this.result));
	}

	@Test
	public void subtract() {
		compile("Result := float '1.2' - float '3.45'");

		assertEquals(1.2 - 3.45, definiteValue(this.result));
	}

	@Test
	public void multiply() {
		compile("Result := float '.12' * float '3.456'");

		assertEquals(0.12 * 3.456, definiteValue(this.result));
	}

	@Test
	public void divide() {
		compile("Result := float '6.12' / '2.34'");

		assertEquals(6.12 / 2.34, definiteValue(this.result));
	}

	@Test
	public void divideByZero() {
		compile("Result := float '123.45' / float '0'");

		assertEquals(ValueType.FLOAT, this.result.toObject().getValueType());
		assertEquals(Double.POSITIVE_INFINITY, definiteValue(this.result));
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.result = field("result").getArtifact().materialize();
		assertEquals(ValueType.FLOAT, this.result.getValueType());
		assertTrue(this.result.inherits(
				this.context.getIntrinsics().getFloat()));
	}

}
