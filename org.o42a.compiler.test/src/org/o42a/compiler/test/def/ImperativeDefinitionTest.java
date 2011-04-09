package org.o42a.compiler.test.def;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeDefinitionTest extends CompilerTestCase {

	@Test
	public void conditionalValue() {
		compile(
				"A := integer(",
				"  Condition := `void",
				"  {Condition? = 1. = 0}",
				")",
				"B := a(Condition = false)",
				"C := b");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("b"), Long.class), is(0L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

	@Test
	public void sign() {
		compile(
				"Sign :=> integer(",
				"  Arg :=< integer.",
				"  {",
				"    Arg > 0? = 1",
				"    Arg < 0? = -1",
				"    = 0",
				"  }",
				")",
				"A := sign(Arg = 10)",
				"A1 := A(Arg = -9)",
				"A2 := A1",
				"B := sign(Arg = -10)",
				"C := sign(Arg = 0)");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("a1"), Long.class), is(-1L));
		assertThat(definiteValue(field("a2"), Long.class), is(-1L));
		assertThat(definiteValue(field("b"), Long.class), is(-1L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

	@Test
	public void defaultValue() {
		compile(
				"A := integer(",
				"  Condition := `void",
				"  {Condition? = 1}",
				"  = 0",
				")",
				"B := a(Condition = false)",
				"C := b");

		assertThat(definiteValue(field("a"), Long.class), is(1L));
		assertThat(definiteValue(field("b"), Long.class), is(0L));
		assertThat(definiteValue(field("c"), Long.class), is(0L));
	}

}
