/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


public class KeepValueTest extends CompilerTestCase {

	@Test
	public void keepValue() {
		compile(
				"A := 5",
				"B := \\\\a");

		assertThat(
				definiteValue(field("b"), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void keepLink() {
		compile(
				"A := `5",
				"B := \\\\a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void linkByValue() {
		compile(
				"A := 5",
				"B := `\\\\a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void linkByAdapterValue() {
		compile(
				"A := 5",
				"B := (`string) \\\\a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.STRING),
				is("5"));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getString()));
	}

	@Test
	public void localFieldDefinition() {
		compile(
				"A := 5",
				"B := integer ({",
				"  L := \\\\a",
				"  = L",
				"})");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(5L));
	}

	@Test
	public void linkByLink() {
		compile(
				"A := `5",
				"B := \\\\a");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void keepLinkTarget() {
		compile(
				"A := `5",
				"B := \\\\a->");

		assertThat(
				definiteValue(field("b"), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void linkByVariable() {
		compile(
				"A := ``5",
				"B := `\\\\a->");

		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));

	}

}
