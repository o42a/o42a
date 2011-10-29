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
package org.o42a.compiler.test.def;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
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

		final Field<?> a = field("a");

		assertTrueValue(valueOf(a));
		assertThat(definiteValue(a, ValueType.INTEGER), is(1L));

		final Field<?> b = field("b");

		assertTrueValue(valueOf(b));
		assertThat(definiteValue(b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void falseLogicalValue() {
		compile(
				"A := integer(False, = 1).",
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
				"A := integer(False. = 1).",
				"B := A().");

		final Value<?> aValue = valueOf(field("a"));

		assertFalseValue(aValue);
		assertThat(aValue.getCompilerValue(), nullValue());

		final Value<?> bValue = valueOf(field("b"));

		assertFalseValue(bValue);
		assertThat(bValue.getCompilerValue(), nullValue());
	}

	@Test
	public void runtimeLogicalValue() {
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
