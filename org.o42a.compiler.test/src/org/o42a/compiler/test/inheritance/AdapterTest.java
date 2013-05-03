/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;


public class AdapterTest extends CompilerTestCase {

	private Obj adapterType;
	private Obj a;

	@Test
	public void adapter() {
		compile(
				"Adapter :=> void (Foo := 1)",
				"A := void (@Adapter := adapter)");

		final Ref adapter = this.a.selfRef().adapt(
				this.a,
				this.adapterType.selfRef().toStaticTypeRef());

		assertThat(adapter, notNullValue());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.adapterType = field("adapter").toObject();
		this.a = field("a").toObject();
	}

}
