/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.declarative;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class DefinitionConditionTest extends CompilerTestCase {

	@Test
	public void truePrecondition() {
		compile(
				"A := integer(Void(), = 1).",
				"B := A().");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void trueCondition() {
		compile(
				"A := integer(Void(). = 1).",
				"B := A().");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void falsePrecondition() {
		compile(
				"A := integer(Void(False), = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertFalseValue(aValue);
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertFalseValue(bValue);
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void falseCondition() {
		compile(
				"A := integer(Void(False). = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertFalseValue(aValue);
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertFalseValue(bValue);
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void runtimePrecondition() {
		compile(
				"Use namespace 'Test'.",
				"A := integer(Rt-void, = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertFalse(aValue.getKnowledge().isKnownToCompiler());
		assertThat(aValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertFalse(aValue.getKnowledge().getCondition().isConstant());
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertFalse(bValue.getKnowledge().isKnownToCompiler());
		assertThat(bValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertFalse(bValue.getKnowledge().getCondition().isConstant());
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void runtimeCondition() {
		compile(
				"Use namespace 'Test'.",
				"A := integer(Rt-void. = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertFalse(aValue.getKnowledge().isKnownToCompiler());
		assertThat(aValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertFalse(aValue.getKnowledge().getCondition().isConstant());
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertFalse(bValue.getKnowledge().isKnownToCompiler());
		assertThat(bValue.getKnowledge().getCondition(), is(Condition.RUNTIME));
		assertFalse(bValue.getKnowledge().getCondition().isConstant());
		assertThat(bValue.getCompilerValue(), nullValue());
	}

}
