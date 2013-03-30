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


public class LocalVisibilityTest extends CompilerTestCase {

	@Test
	public void notVisibleBeforeDeclaration() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  L, $l := 2",
				")");
	}

	@Test
	public void notVisibleFromAnotherAlt() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  $L := 2; = l",
				")");
	}

	@Test
	public void notVisibleAfterMultiAlt() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  $L := 2; void",
				"  = L",
				")");
	}

	@Test
	public void visibleAfterConjunction() {
		compile(
				"A := integer (",
				"  $L := 2, void",
				"  = L",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void localFromPrerequisiteNotVisibleOutsidePrerequisitedSentence() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  $L := 2? void",
				"  = L",
				")");
	}

	@Test
	public void localFromPrerequisitesSentenceNotVisibleOutside() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  Void? $L := 2",
				"  = L",
				")");
	}

	@Test
	public void fieldCantReferLocal() {
		expectError("compiler.unresolved");

		compile(
				"$Local := 1",
				"A := local");
	}

	@Test
	public void fieldDefinitionCantReferLocal() {
		expectError("compiler.unresolved");

		compile(
				"$Local := 1",
				"A := integer (= local)");
	}

	@Test
	public void fieldOfFieldCantReferLocal() {
		expectError("compiler.unresolved");

		compile(
				"$Local := 1",
				"A := void (",
				"  B := local",
				")");
	}

	@Test
	public void nestedObjectFieldCanReferLocal() {
		compile(
				"A := integer (",
				"  $Local := 1",
				"  = Integers: add (",
				"    Left operand = 1",
				"    Right operand = $local",
				"  )",
				")");

		assertThat(definiteValue(field("a"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void localScopeNotPropagated() {
		expectError("compiler.unresolved");

		compile(
				"A := integer (",
				"  1 $ local: local",
				"  = $local",
				")");
	}

}
