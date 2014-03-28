/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class DeclarativeDefinitionsTest extends CompilerTestCase {

	@Test
	public void definiteValue() {
		compile(
				"A := integer(= 1).",
				"B := A().");

		final Field a = field("a");

		assertThat(valueOf(a), trueValue());
		assertThat(definiteValue(a, ValueType.INTEGER), is(1L));

		final Field b = field("b");

		assertThat(valueOf(b), trueValue());
		assertThat(definiteValue(b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void falsePrecondition() {
		compile(
				"A := integer(False, = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertThat(aValue, falseValue());
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertThat(bValue, falseValue());
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void falseCondition() {
		compile(
				"A := integer(False. = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertThat(aValue, falseValue());
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertThat(bValue, falseValue());
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void runtimePrecondition() {
		compile(
				"Use namespace 'Test'.",
				"A := integer(Rt-void, = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertThat(aValue, runtimeValue());
		assertThat(aValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertThat(
				aValue.getKnowledge().getCondition().isConstant(),
				is(false));
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertThat(bValue, runtimeValue());
		assertThat(bValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertThat(
				bValue.getKnowledge().getCondition().isConstant(),
				is(false));
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void runtimeCondition() {
		compile(
				"Use namespace 'Test'.",
				"A := integer(Rt-void. = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertThat(aValue, runtimeValue());
		assertThat(aValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertThat(
				aValue.getKnowledge().getCondition().isConstant(),
				is(false));
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertThat(bValue, runtimeValue());
		assertThat(bValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertThat(
				bValue.getKnowledge().getCondition().isConstant(),
				is(false));
		assertThat(bValue.getCompilerValue(), nullValue());
	}

}
