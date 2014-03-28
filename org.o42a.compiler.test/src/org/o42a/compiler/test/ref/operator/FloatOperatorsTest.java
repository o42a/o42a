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


public strictfp class FloatOperatorsTest extends CompilerTestCase {

	private Obj result;

	@Test
	public void plus() {
		compile("Result := +float '123.45'");

		assertThat(definiteValue(this.result, ValueType.FLOAT), is(123.45));
	}

	@Test
	public void minus() {
		compile("Result := -float '123.45'");

		assertThat(definiteValue(this.result, ValueType.FLOAT), is(-123.45));
	}

	@Test
	public void add() {
		compile("Result := float '1.2' + float '3.4'");

		assertThat(definiteValue(this.result, ValueType.FLOAT), is(1.2 + 3.4));
	}

	@Test
	public void subtract() {
		compile("Result := float '1.2' - float '3.45'");

		assertThat(definiteValue(this.result, ValueType.FLOAT), is(1.2 - 3.45));
	}

	@Test
	public void multiply() {
		compile("Result := float '.12' * float '3.456'");

		assertThat(
				definiteValue(this.result, ValueType.FLOAT),
				is(0.12 * 3.456));
	}

	@Test
	public void divide() {
		compile("Result := float '6.12' / float '2.34'");

		assertThat(
				definiteValue(this.result, ValueType.FLOAT),
				is(6.12 / 2.34));
	}

	@Test
	public void divideByZero() {
		compile("Result := float '123.45' / float '0'");

		assertThat(
				this.result.type().getValueType(),
				valueType(ValueType.FLOAT));
		assertThat(
				definiteValue(this.result, ValueType.FLOAT),
				is(Double.POSITIVE_INFINITY));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.result = field("result").toObject();
		assertThat(
				this.result.type().getValueType(),
				valueType(ValueType.FLOAT));
		assertThat(
				this.result.type().inherits(
						this.context.getIntrinsics().getFloat().type()),
				is(true));
	}

}
