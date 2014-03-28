/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class IntegerOperatorsTest extends CompilerTestCase {

	private Obj result;

	@Test
	public void plus() {
		compile("Result := +2");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(2L));
	}

	@Test
	public void minus() {
		compile("Result := -2");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(-2L));
	}

	@Test
	public void add() {
		compile("Result := 2 + 3");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(5L));
	}

	@Test
	public void subtract() {
		compile("Result := 2 - 3");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(-1L));
	}

	@Test
	public void multiply() {
		compile("Result := 2 * 3");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(6L));
	}

	@Test
	public void divide() {
		compile("Result := 6 / 2");

		assertThat(definiteValue(this.result, ValueType.INTEGER), is(3L));
	}

	@Test
	public void divideByZero() {
		expectError("compiler.arithmetic_error");

		compile("Result := 1 / 0");

		assertThat(
				this.result.type().getValueType(),
				valueType(ValueType.INTEGER));
		assertThat(valueOf(this.result), falseValue());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.result = field("result").toObject();

		assertThat(
				this.result.type().getValueType(),
				valueType(ValueType.INTEGER));
		assertThat(
				this.result.type().inherits(
						this.context.getIntrinsics().getInteger().type()),
				is(true));
	}

}
