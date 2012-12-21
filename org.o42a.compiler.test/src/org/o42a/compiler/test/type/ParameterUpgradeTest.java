/*
    Compiler Tests
    Copyright (C) 2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
