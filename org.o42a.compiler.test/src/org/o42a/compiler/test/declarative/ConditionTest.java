/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ConditionTest extends CompilerTestCase {

	@Test
	public void condition() {
		compile(
				"A := void (",
				"  Condition := void",
				"  Value := void (Condition)",
				")",
				"B := a (Condition = false)",
				"C := b");

		assertThat(
				definiteValue(field("a", "value"), ValueType.VOID),
				voidValue());
		assertThat(valueOf(field("b", "value"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("c", "value"), ValueType.VOID), falseValue());
	}

	@Test
	public void not() {
		compile(
				"A := void (",
				"  Condition := void` link = false",
				"  Value := void (--Condition->)",
				")",
				"B := a (Condition = void)",
				"C := b");

		assertThat(
				definiteValue(field("a", "value"), ValueType.VOID),
				voidValue());
		assertThat(valueOf(field("b", "value"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("c", "value"), ValueType.VOID), falseValue());
	}

	@Test
	public void interrogationSucceed() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = Void. = False",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertThat(
				definiteValue(field("a"), ValueType.VOID),
				voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
		assertThat(valueOf(field("c"), ValueType.VOID), falseValue());
	}

	@Test
	public void interrogationFailed() {
		compile(
				"A := void (",
				"  Condition := 1",
				"  Condition > 0? = False. = Void",
				")",
				"B := a (Condition = 0)",
				"C := b");

		assertThat(valueOf(field("a"), ValueType.VOID), falseValue());
		assertThat(
				definiteValue(field("b"), ValueType.VOID),
				voidValue());
		assertThat(
				definiteValue(field("c"), ValueType.VOID),
				voidValue());
	}

}
