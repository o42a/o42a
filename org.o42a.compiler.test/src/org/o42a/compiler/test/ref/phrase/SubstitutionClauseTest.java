/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class SubstitutionClauseTest extends CompilerTestCase {

	@Test
	public void topLevelSubstitution() {
		compile(
				"A := void (",
				"  <[Arg]>",
				")",
				"B := a [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void returnValue() {
		compile(
				"A := integer (",
				"  <[Arg]> ()",
				")",
				"B := a [2]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

	@Test
	public void overrider() {
		compile(
				"Val := 1",
				"A :=> integer (",
				"  Foo :=< integer` link",
				"  <[Arg]> Foo = ()",
				")",
				"B := a [val]");

		final Field bFoo = field("b", "foo");
		final Obj bFooTarget = linkTarget(bFoo);

		assertThat(definiteValue(bFooTarget, ValueType.INTEGER), is(1L));
		assertThat(
				bFooTarget.getWrapped(),
				sameInstance(field("val").toObject()));
	}

}
