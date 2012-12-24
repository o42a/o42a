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

		assertEquals(ValueType.INTEGER, this.result.type().getValueType());
		assertFalseValue(valueOf(this.result));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.result = field("result").toObject();
		assertEquals(ValueType.INTEGER, this.result.type().getValueType());
		assertTrue(this.result.type().inherits(
				this.context.getIntrinsics().getInteger().type()));
	}

}
