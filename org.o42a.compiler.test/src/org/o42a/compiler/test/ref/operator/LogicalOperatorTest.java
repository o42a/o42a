/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LogicalOperatorTest extends CompilerTestCase {

	@Test
	public void isTrue() {
		compile("A := ++1. B := ++false");

		assertThat(definiteValue(field("a"), ValueType.VOID), voidValue());
		assertThat(valueOf(field("b"), ValueType.VOID), falseValue());
	}

	@Test
	public void not() {
		compile("A := --1. B := --false");

		assertThat(valueOf(field("a"), ValueType.VOID), falseValue());
		assertThat(definiteValue(field("b"), ValueType.VOID), voidValue());
	}

}
