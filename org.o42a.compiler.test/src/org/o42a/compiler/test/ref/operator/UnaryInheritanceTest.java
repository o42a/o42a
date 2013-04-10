/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class UnaryInheritanceTest extends CompilerTestCase {

	private Field aBar;
	private Field bBar;
	private Field cBar;

	@Test
	public void plus() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  Bar := +foo",
				")",
				"B := a (Foo = 2)",
				"C := b");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(2L));
	}

	@Test
	public void minus() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  Bar := -foo.",
				")",
				"B := a (Foo = 2)",
				"C := b");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(-1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(-2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(-2L));
	}

	@Test
	public void valueOf() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  Bar := \\foo",
				")",
				"B := a (Foo = 2)",
				"C := b");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(2L));
	}

	@Test
	public void keepValue() {
		compile(
				"A := void (",
				"  Foo := 1",
				"  Bar := \\\\foo",
				")",
				"B := a (Foo = 2)",
				"C := b");

		assertThat(definiteValue(this.aBar, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(this.bBar, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(this.cBar, ValueType.INTEGER), is(2L));
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aBar = field("a", "bar");
		this.bBar = field("b", "bar");
		this.cBar = field("c", "bar");
	}

}
