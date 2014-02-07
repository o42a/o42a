/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class ResolutionTest extends CompilerTestCase {

	@Test
	public void resolveBeforeUse() {
		compile(
				"A := void",
				"A ()");
	}

	@Test
	public void adapterId() {
		compile(
				"A := void",
				"@A := * ()");
	}

	@Test
	public void namespaceRef() {
		compile(
				"Use namespace 'Console'",
				"Print 'Hello'");
	}

	@Test
	public void adapterIdFromNamespace() {
		compile(
				"Use namespace 'Console'",
				"@Main := * ()");
	}

	@Test
	public void referIncluded() {
		addSource(
				"a",
				"A := integer",
				"============",
				"= 24");
		compile("B := a (= 44)");

		final Field a = field("a");
		final Field b = field("b");

		assertThat(definiteValue(a, ValueType.INTEGER), is(24L));
		assertThat(definiteValue(b, ValueType.INTEGER), is(44L));
	}

	@Test
	public void fieldWithTheSameNameAsOwner() {
		compile(
				"A := integer (",
				"  A := 2",
				"  = 1",
				"  B := a",
				")");

		assertThat(definiteValue(field("a", "b"), ValueType.INTEGER), is(2L));
	}

}
