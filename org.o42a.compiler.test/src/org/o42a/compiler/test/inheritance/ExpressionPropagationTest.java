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
import org.o42a.core.value.ValueType;


public class ExpressionPropagationTest extends CompilerTestCase {

	@Test
	public void callPropagation() {
		compile(
				"A := void(",
				"  Value := integer(= 1).",
				"  Foo := integer(",
				"    = Value().",
				"  ).",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field a = field("a");
		final Field b = field("b");
		final Field c = field("c");

		final Field aFoo = field(a, "foo");
		final Field bFoo = field(b, "foo");
		final Field cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void fieldCallPropagation() {
		compile(
				"A := void(",
				"  Container := void(",
				"    Value := integer(= 1).",
				"  ).",
				"  Foo := integer(",
				"    = Container: value().",
				"  ).",
				").",
				"B := A(",
				"  Container = *(",
				"    Value = *(= 2).",
				"  ).",
				").",
				"C := B.");

		final Field a = field("a");
		final Field b = field("b");
		final Field c = field("c");

		final Field aFoo = field(a, "foo");
		final Field bFoo = field(b, "foo");
		final Field cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void scopeDependentFieldInheritance() {
		compile(
				"A := void(",
				"  Value := integer(",
				"    = 1.",
				"    Field := integer(= value).",
				"  ).",
				"  Foo := value: field().",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field a = field("a");
		final Field b = field("b");
		final Field c = field("c");

		final Field aFoo = field(a, "foo");
		final Field bFoo = field(b, "foo");
		final Field cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(2L));
	}

	@Test
	public void scopeDependentFieldCallPropagation() {
		compile(
				"A := void(",
				"  Value := integer(",
				"    = 1.",
				"    Field := integer(= value).",
				"  ).",
				"  Foo := integer(",
				"    = Value: field().",
				"  ).",
				").",
				"B := A(",
				"  Value = *(= 2).",
				").",
				"C := B.");

		final Field a = field("a");
		final Field b = field("b");
		final Field c = field("c");

		final Field aFoo = field(a, "foo");
		final Field bFoo = field(b, "foo");
		final Field cFoo = field(c, "foo");

		assertThat(definiteValue(aFoo, ValueType.INTEGER), is(1L));
		assertThat(definiteValue(bFoo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(cFoo, ValueType.INTEGER), is(2L));
	}

}
