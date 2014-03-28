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


public class FloatCompareTest extends CompilerTestCase {

	@Test
	public void less() {
		assertThat(
				definiteValue(
						compare("Result := float '1.1' < float '1.2'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '1.2' < float '1.1'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void lessOrEqual() {
		assertThat(
				definiteValue(
						compare("Result := float '0.1' <= float '0.2'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '0.2' <= float '0.1'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void greater() {
		assertThat(
				definiteValue(
						compare("Result := float '0.2' > float '0.1'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '0.1' > float '0.2'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void greaterOrEqual() {
		assertThat(
				definiteValue(
						compare("Result := float '0.2' >= float '0.1'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '0.1' >= float '0.2'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void equal() {
		assertThat(
				definiteValue(
						compare("Result := float '0.2' == float '0.2'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '0.1' == float '0.2'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void notEqual() {
		assertThat(
				definiteValue(
						compare("Result := float '0.1' <> float '0.2'"),
						ValueType.VOID),
				voidValue());
		assertThat(
				valueOf(
						compare("Result := float '0.1' <> float '0.1'"),
						ValueType.VOID),
				falseValue());
	}

	@Test
	public void compare() {
		assertThat(
				definiteValue(
						compare("Result := float '0.1' <=> float '-0.1'"),
						ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(
						compare("Result := float '-0.1' <=> float '0.1'"),
						ValueType.INTEGER),
				is(-1L));
		assertThat(
				definiteValue(
						compare("Result := float '10.2' <=> float '10.2'"),
						ValueType.INTEGER),
				is(0L));
	}

	private Obj compare(String line, String... lines) {
		compile(line, lines);
		return field("result").toObject();
	}

}
