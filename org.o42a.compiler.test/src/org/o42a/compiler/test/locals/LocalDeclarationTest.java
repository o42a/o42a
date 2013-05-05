/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.locals;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.ValueKnowledge.VARIABLE_VALUE;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class LocalDeclarationTest extends CompilerTestCase {

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

	@Test
	public void localLink() {
		compile(
				"A := \"123\"",
				"B := link (`string) (",
				"  $Local := `a",
				"  = $local`",
				")");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
		assertThat(
				linkTarget(field("b")).getWrapped().getWrapped(),
				is(field("a").toObject()));
	}

	@Test
	public void linkTarget() {
		compile(
				"A := `\"123\"",
				"B := link (`string) (",
				"  $Local := a",
				"  = $local->",
				")");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
		assertThat(
				linkTarget(field("b")).getWrapped().getWrapped(),
				is(linkTarget(field("a")).getWrapped()));
	}

	@Test
	public void linkBody() {
		compile(
				"A := `\"123\"",
				"B := link (`string) (",
				"  $Local := a",
				"  = $local",
				")");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("123"));
		assertThat(
				linkTarget(field("b")).getWrapped().getWrapped(),
				is(linkTarget(field("a")).getWrapped()));
	}

	@Test
	public void localVariable() {
		compile(
				"A := \"123\"",
				"B := variable (`string) (",
				"  $Local := ``a",
				"  = $local",
				")");

		final Value<?> value = field("b").toObject().value().getValue();

		assertThat(value.getKnowledge(), is(VARIABLE_VALUE));
	}

}
