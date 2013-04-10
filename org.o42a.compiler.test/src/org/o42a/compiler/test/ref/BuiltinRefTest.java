/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;


public class BuiltinRefTest extends CompilerTestCase {

	@Test
	public void voidRef() {
		compile("A := `void");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.context.getVoid()));
		assertTrueVoid(aTarget);
	}

	@Test
	public void rootVoidRef() {
		compile("A := `//void");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.context.getVoid()));
		assertTrueVoid(aTarget);
	}

	@Test
	public void falseRef() {
		compile("A := `false");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.context.getFalse()));
		assertFalseVoid(aTarget);
	}

	@Test
	public void rootFalseRef() {
		compile("A := `//false");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.context.getFalse()));
		assertFalseVoid(aTarget);
	}

}
