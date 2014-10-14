/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


public class EagerRefTest extends CompilerTestCase {

	@Test
	public void eagerValue() {
		compile(
				"A := 5",
				"B := a>>");

		assertThat(
				definiteValue(field("b"), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void eagerLink() {
		compile(
				"A := `5",
				"B := a>>");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void linkByValue() {
		compile(
				"A := 5",
				"B := `a>>");

		assertThat(
				definiteValue(linkTarget(field("b")), ValueType.INTEGER),
				is(5L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType(),
				is(field("a").toObject()));
	}

	@Test
	public void linkByAdapterValue() {
		compile(
				"A := 5",
				"B := string` link = a>>");

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
		expectError("compiler.invalid_local_declarable");

		compile(
				"A := 5",
				"B := integer ({",
				"  L := a>>",
				"  = L",
				"})");

		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(5L));
	}

	@Test
	public void linkByLink() {
		compile(
				"A := `5",
				"B := a>>");

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
	public void eagerLinkTarget() {
		compile(
				"A := `5",
				"B := a->>>");

		assertThat(
				definiteValue(field("b"), ValueType.INTEGER),
				is(5L));
	}

	@Test
	public void linkByVariable() {
		compile(
				"A := ``5",
				"B := `a->>>");

		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b").toObject().type().getParameters())
				.getType()
				.type()
				.getValueType(),
				valueType(ValueType.INTEGER));
	}

	@Test
	public void eagerIneritance() {
		expectError("compiler.prohibited_eager_inheritance");

		compile(
				"A := 5",
				"B := a>> ()");
	}

	@Test
	public void eagerPropagation() {
		compile(
				"A := void (",
				"  F := 5",
				"  G := f>>",
				")",
				"B := a (*F (= 6))");

		assertThat(definiteValue(field("a", "g"), ValueType.INTEGER), is(5L));
		assertThat(definiteValue(field("b", "g"), ValueType.INTEGER), is(6L));
	}

	@Test
	public void eagerOverride() {
		expectError("compiler.prohibited_eager_override");

		compile(
				"A := void (F := 5>>)",
				"B := a (*F)");
	}

}
