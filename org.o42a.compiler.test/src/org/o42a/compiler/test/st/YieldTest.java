/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.st;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class YieldTest extends CompilerTestCase {

	@Test
	public void yield() {
		compile(
				"A := integer (",
				"  << 1",
				")");

		assertThat(valueOf(field("a"), ValueType.INTEGER), runtimeValue());
	}

	@Test
	public void yieldTwice() {
		compile(
				"A := integer (",
				"  << 1, << 2",
				")");

		assertThat(valueOf(field("a"), ValueType.INTEGER), runtimeValue());
	}

}
