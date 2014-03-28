/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


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

		assertThat(definiteValue(this.aBar, ValueType.VOID), voidValue());
		assertThat(valueOf(this.bBar, ValueType.VOID), falseValue());
		assertThat(valueOf(this.cBar, ValueType.VOID), falseValue());
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

		assertThat(valueOf(this.aBar, ValueType.VOID), falseValue());
		assertThat(definiteValue(this.bBar, ValueType.VOID), voidValue());
		assertThat(definiteValue(this.cBar, ValueType.VOID), voidValue());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.aBar = field("a", "bar").toObject();
		this.bBar = field("b", "bar").toObject();
		this.cBar = field("c", "bar").toObject();
	}

}
