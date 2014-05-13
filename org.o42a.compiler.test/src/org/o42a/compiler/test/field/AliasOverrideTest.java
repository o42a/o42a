/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.o42a.analysis.use.User.dummyUser;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class AliasOverrideTest extends CompilerTestCase {

	@Test
	public void overrideFieldByAlias() {
		compile(
				"A := void (",
				"  F := 1",
				"  G :- f",
				")",
				"B := a (",
				"  G = 2",
				")");

		assertThat(definiteValue(field("b", "g"), ValueType.INTEGER), is(2L));
		assertThat(definiteValue(field("b", "f"), ValueType.INTEGER), is(2L));
		assertThat(field("b", "g"), sameInstance(field("b", "f")));
	}

	@Test
	public void prohibitRefAliasOverride() {
		expectError("compiler.prohibited_alias_override");
		compile(
				"A := integer (",
				"  F :- ::",
				"  = 1",
				")",
				"B := a (",
				"  F = 2",
				")");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
		assertThat(definiteValue(
				member("b", "f").substance(dummyUser()).toObject(),
				ValueType.INTEGER),
				is(1L));
	}

	@Test
	public void prohibitExpressionAliasOverride() {
		expectError("compiler.prohibited_alias_override");
		compile(
				"A := void (",
				"  F :- 1",
				")",
				"B := a (",
				"  F = 2",
				")");

		assertThat(definiteValue(field("b", "f"), ValueType.INTEGER), is(1L));
	}

}
