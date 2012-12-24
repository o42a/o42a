/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


public class ParameterUpgradeTest extends CompilerTestCase {

	@Test
	public void linkByLink() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< link (`#t)",
				"  G := `f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				definiteValue(linkTarget(field("b", "g")), ValueType.INTEGER),
				is(23L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
						.getType(),
						is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void linkByVariable() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< variable (`#t)",
				"  G := `f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void linkValue() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< link (`#t)",
				"  G := `/f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				definiteValue(linkTarget(field("b", "g")), ValueType.INTEGER),
				is(23L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
						.getType(),
						is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void variableValue() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< variable (`#t)",
				"  G := `/f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void keepLink() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< link (`#t)",
				"  G := `//f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				definiteValue(linkTarget(field("b", "g")), ValueType.INTEGER),
				is(23L));
		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
						.getType(),
						is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void keepVariable() {
		compile(
				"A :=> void #(",
				"  T := void",
				") (",
				"  F :=< variable (`#t)",
				"  G := `//f",
				")",
				"B := a (`integer) (",
				"  F = 23",
				")");

		assertThat(
				LinkValueType.LINK.interfaceRef(
						field("b", "g").toObject().type().getParameters())
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

}
