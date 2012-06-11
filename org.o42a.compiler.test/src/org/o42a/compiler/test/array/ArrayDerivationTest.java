/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayItem;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.value.ValueType;


public class ArrayDerivationTest extends CompilerTestCase {

	@Test
	public void inherit() {
		compile(
				"A := [1, 2, 3]",
				"B := a");

		final Obj b = field("b").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) b.value().getValueStruct();

		assertFalse(arraySruct.isVariable());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(),
				is(b.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertThat(
				definiteValue(items[0].getTarget(), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(items[1].getTarget(), ValueType.INTEGER),
				is(2L));
		assertThat(
				definiteValue(items[2].getTarget(), ValueType.INTEGER),
				is(3L));
	}

	@Test
	public void deriveField() {
		compile(
				"A := void(",
				"  Field := [1, 2, 3]",
				")",
				"B := a");

		final Obj bField = field("b", "field").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) bField.value().getValueStruct();

		assertFalse(arraySruct.isVariable());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(),
				is(bField.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(bField);
		final ArrayItem[] items = array.items(bField.getScope());

		assertThat(items.length, is(3));
		assertThat(
				definiteValue(items[0].getTarget(), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(items[1].getTarget(), ValueType.INTEGER),
				is(2L));
		assertThat(
				definiteValue(items[2].getTarget(), ValueType.INTEGER),
				is(3L));
	}

	@Test
	public void expressionArrayItem() {
		compile(
				"A := void(",
				"  F := `1",
				"  G := `2",
				"  Field := row (`integer) [[f + g]]",
				")",
				"B := a(F = 2. G = 4)");

		final Obj bField = field("b", "field").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) bField.value().getValueStruct();

		assertFalse(arraySruct.isVariable());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(),
				is(bField.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(bField);
		final ArrayItem[] items = array.items(bField.getScope());

		assertThat(items.length, is(1));
		assertThat(
				definiteValue(items[0].getTarget(), ValueType.INTEGER),
				is(6L));
	}

}
