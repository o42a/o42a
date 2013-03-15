/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ComparisonMacroTest extends CompilerTestCase {

	@Test
	public void eq() {
		compile("A := 2 ##eq [2]");

		assertTrueVoid(field("a"));
	}

}
