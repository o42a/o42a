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
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;


public class DeclarativeDefinitionsTest extends CompilerTestCase {

	@Test
	public void definiteValue() {
		compile(
				"A := integer(= 1).",
				"B := A().");

		final Field<?> a = field("a");

		assertTrueValue(a.getArtifact().materialize().getValue());
		assertThat(definiteValue(a, Long.class), is(1L));

		final Field<?> b = field("b");

		assertTrueValue(b.getArtifact().materialize().getValue());
		assertThat(definiteValue(b, Long.class), is(1L));
	}

	@Test
	public void falseLogicalValue() {
		compile(
				"A := integer(False, = 1).",
				"B := A().");

		final Field<?> a = field("a");
		final Value<?> aValue = a.getArtifact().materialize().getValue();

		assertFalseValue(aValue);
		assertThat(aValue.getDefiniteValue(), nullValue());

		final Field<?> b = field("b");
		final Value<?> bValue = b.getArtifact().materialize().getValue();

		assertFalseValue(bValue);
		assertThat(bValue.getDefiniteValue(), nullValue());
	}

	@Test
	public void falseCondition() {
		compile(
				"A := integer(False. = 1).",
				"B := A().");

		final Field<?> a = field("a");
		final Value<?> aValue = a.getArtifact().materialize().getValue();

		assertFalseValue(aValue);
		assertThat(aValue.getDefiniteValue(), nullValue());

		final Field<?> b = field("b");
		final Value<?> bValue = b.getArtifact().materialize().getValue();

		assertFalseValue(bValue);
		assertThat(bValue.getDefiniteValue(), nullValue());
	}

	@Test
	public void runtimeLogicalValue() {
		compile(
				"Use namespace 'Console'.",
				"A := integer(Print(Text = \"1\"), = 1).",
				"B := A().");

		final Field<?> a = field("a");
		final Value<?> aValue = a.getArtifact().materialize().getValue();

		assertFalse(aValue.isDefinite());
		assertThat(aValue.getLogicalValue(), is(LogicalValue.RUNTIME));
		assertFalse(aValue.getLogicalValue().isConstant());
		assertThat(aValue.getDefiniteValue(), nullValue());

		final Field<?> b = field("b");
		final Value<?> bValue = b.getArtifact().materialize().getValue();

		assertFalse(bValue.isDefinite());
		assertThat(bValue.getLogicalValue(), is(LogicalValue.RUNTIME));
		assertFalse(bValue.getLogicalValue().isConstant());
		assertThat(bValue.getDefiniteValue(), nullValue());
	}

	@Test
	public void runtimeCondition() {
		compile(
				"Use namespace 'Console'.",
				"A := integer(Print(Text = \"1\"). = 1).",
				"B := A().");

		final Field<?> a = field("a");
		final Value<?> aValue = a.getArtifact().materialize().getValue();

		assertFalse(aValue.isDefinite());
		assertThat(aValue.getLogicalValue(), is(LogicalValue.RUNTIME));
		assertFalse(aValue.getLogicalValue().isConstant());
		assertThat(aValue.getDefiniteValue(), nullValue());

		final Field<?> b = field("b");
		final Value<?> bValue = b.getArtifact().materialize().getValue();

		assertFalse(bValue.isDefinite());
		assertThat(bValue.getLogicalValue(), is(LogicalValue.RUNTIME));
		assertFalse(bValue.getLogicalValue().isConstant());
		assertThat(bValue.getDefiniteValue(), nullValue());
	}

}
