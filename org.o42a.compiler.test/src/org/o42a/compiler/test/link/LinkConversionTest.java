/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LinkConversionTest extends CompilerTestCase {

	@Test
	public void convertToString() {
		compile(
				"A :=> void (L :=< link (`string))",
				"B := `2",
				"C := a (L = b)");

		assertThat(
				definiteValue(linkTarget(field("c", "l")), ValueType.STRING),
				is("2"));
	}

}
