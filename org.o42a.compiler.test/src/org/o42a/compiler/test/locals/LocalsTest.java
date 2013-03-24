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


public class LocalsTest extends CompilerTestCase {

	@Test
	public void fieldRefersLocalFromSeparateSentence() {
		compile(
				"$L := 1",
				"A := l");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void declareInSeparateSentence() {
		compile(
				"A := integer (",
				"  $L := 1",
				"  = l",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void declareInConjunction() {
		compile(
				"A := integer (",
				"  $L := 1, = l",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void declareInPrerequisite() {
		compile(
				"A := integer (",
				"  $L := 1? = L",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(1L));
	}

}
