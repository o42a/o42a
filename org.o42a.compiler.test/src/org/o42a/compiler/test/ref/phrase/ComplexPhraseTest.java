/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ComplexPhraseTest extends CompilerTestCase {

	@Test
	public void phraseAsPhrasePrefix() {
		compile("A := (integer '5') '7'");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(7L));
	}

	@Test
	public void phraseAsPhrasePrefix2() {
		compile(
				"Str :=> string(",
				"  <''> ()",
				")",
				"A := (str 'abc') [1]");

		assertThat(definiteValue(field("a"), ValueType.STRING), is("b"));
	}

	@Test
	public void interrogativeClause() {
		compile(
				"Is :=> string (",
				"  <*Check> (<[Condition]>? = \"true\". = \"false\")",
				")",
				"A := is [1 > 0]",
				"B := is [1 < 0]");

		assertThat(definiteValue(field("a"), ValueType.STRING), is("true"));
		assertThat(definiteValue(field("b"), ValueType.STRING), is("false"));
	}

}
