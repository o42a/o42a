/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LinkCastTest extends CompilerTestCase {

	@Test
	public void linkToValue() {
		compile(
				"A := `123",
				"B := string(= A #cast)");

		assertThat(definiteValue(field("b"), ValueType.STRING), is("123"));
	}

	@Test
	public void linkToLink() {
		compile(
				"A := `123",
				"B := string` link = a# cast");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
	}

	@Test
	public void propagateCast() {
		compile(
				"To string :=> string (",
				"  #T :=< void",
				"  Arg :=< (#t)` link = void",
				"  = Arg #cast",
				")",
				"A := to string(T = integer. Arg = 456)");

		assertThat(definiteValue(field("a"), ValueType.STRING), is("456"));
	}

}
