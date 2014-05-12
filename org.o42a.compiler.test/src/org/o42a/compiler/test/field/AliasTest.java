/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldKind.ALIAS_FIELD;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;


public class AliasTest extends CompilerTestCase {

	@Test
	public void fieldAlias() {
		compile(
				"A := 1",
				"B :- a");

		assertThat(field("b"), sameInstance(field("a")));
	}

	@Test
	public void refAlias() {
		compile(
				"A := void (",
				"  F := 1",
				")",
				"B :- a: f");

		final Member b = member("b");
		final Field af = field("a", "f");

		assertThat(b.toField(), nullValue());
		assertThat(b.substance(dummyUser()), sameInstance(af.toObject()));
	}

	@Test
	public void derefAlias() {
		compile(
				"A := `1",
				"B :- a->");

		final Field b = field("b");

		assertThat(b.getFieldKind(), is(ALIAS_FIELD));
		assertThat(definiteValue(b), is(1L));
	}

	@Test
	public void objectAlias() {
		compile("A :- void (F := 1)");

		assertThat(field("a").getFieldKind(), is(ALIAS_FIELD));
		assertThat(definiteValue(field("a", "f")), is(1L));
	}

	@Test
	public void scopeAlias() {
		compile(
				"A := void (",
				"  F :- ::",
				")");

		final Member af = member("a", "f");

		assertThat(af.toField(), nullValue());
		assertThat(
				af.substance(dummyUser()),
				sameInstance(field("a").toObject()));
	}

}
