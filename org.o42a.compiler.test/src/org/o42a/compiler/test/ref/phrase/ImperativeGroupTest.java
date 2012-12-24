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


public class ImperativeGroupTest extends CompilerTestCase {

	@Test
	public void imperativeGroup() {
		compile(
				"A := integer (",
				"  <*> {",
				"    <[Arg]> ()",
				"  }",
				")",
				"B := A [5]");

		final Field b = field("b");

		assertThat(definiteValue(b, ValueType.INTEGER), is(5L));
	}

}
