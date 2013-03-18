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


public class ModuleRefTest extends CompilerTestCase {

	@Test
	public void moduleRef() {
		compile("A := `module ref");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.module.toObject()));
	}

	@Test
	public void parentModuleRef() {
		compile("A := `parent module ref::");

		final Obj aTarget = linkTarget(field("a"));

		assertThat(aTarget.getWrapped(), is(this.module.toObject()));
	}

}
