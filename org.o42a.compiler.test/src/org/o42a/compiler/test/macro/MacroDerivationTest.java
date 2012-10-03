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
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class MacroDerivationTest extends CompilerTestCase {

	@Test
	public void overrideMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  F := (`#t) 1",
				")",
				"B := a (",
				"  T = integer",
				")");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				af.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				bf.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(true));
	}

	@Test
	public void propagateMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  F := (`#t) 1",
				")",
				"B := a");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				af.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				bf.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(false));
	}

	@Test
	public void propagateDeepMacro() {
		compile(
				"A := void (",
				"  Inner := void (",
				"    #T := void",
				"  )",
				"  F := (`#inner: t) 1",
				")",
				"B := a");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				af.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				bf.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(false));
	}

}
