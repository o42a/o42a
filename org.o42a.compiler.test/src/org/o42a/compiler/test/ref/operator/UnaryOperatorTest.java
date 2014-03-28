/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class UnaryOperatorTest extends CompilerTestCase {

	@Test
	public void plus() {
		compile(
				"A :=> integer (<+*> = 1)",
				"B := + a");

		final Obj b = field("b").toObject();

		assertThat(
				b.type().derivedFrom(
						this.context.getIntrinsics().getInteger().type()),
				is(true));
		assertThat(definiteValue(b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void minus() {
		compile(
				"A :=> integer (<-*> = 1)",
				"B := - a");

		final Obj b = field("b").toObject();

		assertThat(
				b.type().derivedFrom(
						this.context.getIntrinsics().getInteger().type()),
				is(true));
		assertThat(definiteValue(b, ValueType.INTEGER), is(1L));
	}

	@Test
	public void remainSamePlus() {
		compile(
				"A := integer (= 2. <+*>)",
				"B := + a");

		final Obj b = field("b").toObject();

		assertThat(
				b.type().derivedFrom(
						this.context.getIntrinsics().getInteger().type()),
				is(true));
		assertThat(definiteValue(b, ValueType.INTEGER), is(2L));
	}

}
