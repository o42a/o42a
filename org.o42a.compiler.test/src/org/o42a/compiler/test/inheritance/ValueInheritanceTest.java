/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.def.Defs;
import org.o42a.core.value.ValueType;


public class ValueInheritanceTest extends CompilerTestCase {

	private Field a;
	private Field b;

	@Test
	public void inheritValue() {
		compile("A := 1. B := a");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void propagateValue() {
		compile("A := 1. B := a");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void overrideValue() {
		compile(
				"A := 1",
				"B := a (= 2)");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overridePropagatedValue() {
		compile(
				"A := 1",
				"B := &a (= 2)");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void enforceCondition() {
		compile(
				"A := 1",
				"B := a (void)");
		assertThat(definiteValue(this.a, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void enforceByRuntimeCondition() {
		compile(
				"Use namespace 'Test'",
				"A := 1",
				"B := a (Rt-void)");

		final Defs propositions =
				field("b")
				.toObject()
				.value()
				.getDefinitions()
				.defs();

		assertThat(propositions.length(), is(2));
	}

	@Test
	public void enforceVoidByRuntimeCondition() {
		compile(
				"Use namespace 'Test'",
				"A := void",
				"B := a (Rt-void)");

		final Defs propositions =
				field("b")
				.toObject()
				.value()
				.getDefinitions()
				.defs();

		assertThat(propositions.length(), is(2));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a");
		this.b = field("b");
	}

}
