/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class LogicalExpressionDefinitionTest extends CompilerTestCase {

	private Obj a;
	private Obj b;
	private Obj c;

	@Test
	public void isTrue() {
		compile("A := void. B := void(= ++A). C := b.");

		assertTrueVoid(this.a);
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Test
	public void notTrue() {
		compile("A := void. B := void(= --A). C := b.");

		assertTrueVoid(this.a);
		assertFalseVoid(this.b);
		assertFalseVoid(this.c);
	}

	@Test
	public void notFalse() {
		compile("A := false. B := void(= --A). C := b.");

		assertFalseVoid(this.a);
		assertTrueVoid(this.b);
		assertTrueVoid(this.c);
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.a = field("a").toObject();
		this.b = field("b").toObject();
		this.c = field("c").toObject();
	}

}
