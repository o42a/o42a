/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class LogiclInheritanceTest extends CompilerTestCase {

	private Obj aBar;
	private Obj bBar;
	private Obj cBar;

	@Test
	public void isTrue() {
		compile(
				"A := void(",
				"  Foo := void",
				"  Bar := ++foo",
				")",
				"B := a(Foo = false)",
				"C := b");

		assertTrueVoid(this.aBar);
		assertFalseVoid(this.bBar);
		assertFalseVoid(this.cBar);
	}

	@Test
	public void not() {
		compile(
				"A := void(",
				"  Foo := void",
				"  Bar := --foo",
				")",
				"B := a(Foo = false)",
				"C := b");

		assertFalseVoid(this.aBar);
		assertTrueVoid(this.bBar);
		assertTrueVoid(this.cBar);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aBar = field("a", "bar").toObject();
		this.bBar = field("b", "bar").toObject();
		this.cBar = field("c", "bar").toObject();
	}

}
