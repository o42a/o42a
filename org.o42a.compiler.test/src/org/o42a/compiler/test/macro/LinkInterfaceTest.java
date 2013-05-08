/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class LinkInterfaceTest extends CompilerTestCase {

	@Test
	public void linkInterface() {
		compile(
				"A := `1",
				"B := (a #interface)` link = 2");

		assertThat(
				LINK.interfaceRef(field("b").toObject().type().getParameters())
				.getType(),
				is(ValueType.INTEGER.typeObject(this.context.getIntrinsics())));
		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void linkPrototypeInterface() {
		compile(
				"A :=> integer` link",
				"B := (a #interface)` link = 2");

		assertThat(
				LINK.interfaceRef(field("b").toObject().type().getParameters())
				.getType(),
				is(ValueType.INTEGER.typeObject(this.context.getIntrinsics())));
		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(2L));
	}

}
