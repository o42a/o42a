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


public class LogicalDefinitionTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Test
	public void isTrue() {
		compile("A := void. B := ++a. C := b.");

		assertThat(definiteValue(this.a, ValueType.VOID), voidValue());
		assertThat(definiteValue(this.b, ValueType.VOID), voidValue());
		assertThat(definiteValue(this.c, ValueType.VOID), voidValue());
	}

	@Test
	public void notTrue() {
		compile("A := void. B := --a. C := b.");

		assertThat(definiteValue(this.a, ValueType.VOID), voidValue());
		assertThat(valueOf(this.b, ValueType.VOID), falseValue());
		assertThat(valueOf(this.c, ValueType.VOID), falseValue());
	}

	@Test
	public void notFalse() {
		compile("A := false. B := --a. C := b.");

		assertThat(valueOf(this.a, ValueType.VOID), falseValue());
		assertThat(definiteValue(this.b, ValueType.VOID), voidValue());
		assertThat(definiteValue(this.c, ValueType.VOID), voidValue());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
		this.c = field("c").toObject();
	}

}
