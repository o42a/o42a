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


public class LocalScopeTest extends CompilerTestCase {

	@Test
	public void condition() {
		compile(
				"A := void (",
				"  False $ local:",
				"  local",
				")");

		assertFalseVoid(field("a"));
	}

	@Test
	public void value() {
		compile(
				"A := integer (",
				"  3 $ local (= $Local)",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void imperativeLocalScope() {
		compile(
				"A := integer (",
				"  3 $ local {= $Local}",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void localLink() {
		compile(
				"A := \"123\"",
				"B := string` link (",
				"  `a $ Local (",
				"    = $Local",
				"  )",
				")");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
		assertThat(
				linkTarget(field("b")).getWrapped().getWrapped(),
				is(field("a").toObject()));
	}

}
