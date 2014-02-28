/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class PhraseTest extends CompilerTestCase {

	@Test
	public void overriderInsideOverrider() {
		compile(
				"A := void (",
				"  Foo := integer (Bar :=> string. = 1)",
				"  <[]> foo = * (<''> Bar => *)",
				")",
				"B := a [2] 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");
		final Field bar = field(foo, "bar");

		assertThat(definiteValue(foo, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(bar, ValueType.STRING), is("b"));
	}

	@Test
	public void overriderInsideClause() {
		compile(
				"A := integer (",
				"  Foo :=> string",
				"  <[]> a (<''> Foo => string)",
				")",
				"B := a [2] 'b'");

		final Field b = field("b");
		final Field foo = field(b, "foo");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
		assertThat(definiteValue(foo, ValueType.STRING), is("b"));
	}

	@Test
	public void nestedFieldRef() {
		compile(
				"A :=> void (",
				"  Val :=< integer` link",
				"  Sum :=> integer (",
				"    Inc :=< integer` link",
				"    = Val + inc",
				"  )",
				"  <*> Sum (",
				"    <[]> Inc = ()",
				"  )",
				")",
				"B := a (Val = 1)",
				"C := b [10]");

		assertThat(definiteValue(field("c"), ValueType.INTEGER), is(11L));
	}

}
