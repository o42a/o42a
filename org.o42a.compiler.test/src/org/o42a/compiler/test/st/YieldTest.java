/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.st;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class YieldTest extends CompilerTestCase {

	@Test
	public void yield() {
		compile(
				"A := integer (",
				"  << 1",
				")");

		final Value<Long> value = valueOf(field("a"), ValueType.INTEGER);

		assertRuntimeValue(value);
	}

	@Test
	public void yieldTwice() {
		compile(
				"A := integer (",
				"  << 1, << 2",
				")");

		final Value<Long> value = valueOf(field("a"), ValueType.INTEGER);

		assertRuntimeValue(value);
	}

}
