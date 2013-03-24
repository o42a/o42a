/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.locals;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LocalExpressionTest extends CompilerTestCase {

	@Test
	public void expression() {
		compile(
				"A := string (",
				"  $Prefix := \"Hello\"",
				"  = $Prefix + \", World!\"",
				")");

		assertThat(
				definiteValue(field("a"), ValueType.STRING),
				is("Hello, World!"));
	}

}
