/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class RuntimeValueTest extends CompilerTestCase {

	@Test
	public void runtimeInteger() {
		compile(
				"Use namespace 'Test'",
				"A := rt-integer '5'",
				"B := a");

		assertRuntimeInteger(field("a").toObject());
		assertRuntimeInteger(field("b").toObject());
	}

	@Test
	public void runtimeIntegerLink() {
		compile(
				"Use namespace 'Test'",
				"A := integer` link = rt-integer '5'",
				"B := integer` link = a");

		assertRuntimeInteger(linkTarget(field("a")));
		assertRuntimeInteger(linkTarget(field("b")));
	}

	@Test
	public void imperativeRuntimeInteger() {
		compile(
				"Use namespace 'Test'",
				"A := integer({= Rt-integer '5'})",
				"B := a");

		assertRuntimeInteger(field("a").toObject());
		assertRuntimeInteger(field("b").toObject());
	}

	private static void assertRuntimeInteger(Obj object) {
		assertThat(object.type().getValueType(), valueType(ValueType.INTEGER));
		assertThat(object.value().getValue(), runtimeValue());
	}

}
