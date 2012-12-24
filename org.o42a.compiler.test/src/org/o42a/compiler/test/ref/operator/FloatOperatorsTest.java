/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


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
		compile("Result := float '6.12' / float '2.34'");

		assertEquals(6.12 / 2.34, definiteValue(this.result));
	}

	@Test
	public void divideByZero() {
		compile("Result := float '123.45' / float '0'");

		assertEquals(ValueType.FLOAT, this.result.type().getValueType());
		assertEquals(Double.POSITIVE_INFINITY, definiteValue(this.result));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.result = field("result").toObject();
		assertEquals(ValueType.FLOAT, this.result.type().getValueType());
		assertTrue(this.result.type().inherits(
				this.context.getIntrinsics().getFloat().type()));
	}

}
