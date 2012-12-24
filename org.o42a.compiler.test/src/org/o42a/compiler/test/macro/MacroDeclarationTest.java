/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class MacroDeclarationTest extends CompilerTestCase {

	@Test
	public void macroDeclaration() {
		compile(
				"A := 123",
				"#B := a + 1");

		final Obj b = field("b").toObject();

		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroObject() {
		compile(
				"A := 123",
				"B := macro(= A + 1)");

		final Obj b = field("b").toObject();

		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroDeclarationByMacroValue() {
		compile(
				"#A := 123",
				"#B := a");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(a.type().getValueType().isMacro(), is(true));
		assertThat(b.type().getValueType().isMacro(), is(true));
	}

	@Test
	public void macroObjectByMacroValue() {
		compile(
				"#A := 123",
				"B := macro(= A)");

		final Obj a = field("a").toObject();
		final Obj b = field("b").toObject();

		assertThat(a.type().getValueType().isMacro(), is(true));
		assertThat(b.type().getValueType().isMacro(), is(true));
	}

}
