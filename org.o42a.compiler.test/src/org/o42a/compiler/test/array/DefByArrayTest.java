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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueStruct;


public class DefByArrayTest extends CompilerTestCase {

	@Test
	public void buildRow() {
		compile(
				"A := [\"a\", \"b\", \"c\"]",
				"B := row (`string) (",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) b.value().getValueStruct();

		assertFalse(arraySruct.isVariable());
		assertThat(
				arraySruct.getItemTypeRef().getType(),
				is(b.getContext().getIntrinsics().getString()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertThat(
				definiteValue(items[0].getTarget(), ValueType.STRING),
				is("a"));
		assertThat(
				definiteValue(items[1].getTarget(), ValueType.STRING),
				is("b"));
		assertThat(
				definiteValue(items[2].getTarget(), ValueType.STRING),
				is("c"));
	}

	@Test
	public void buildArray() {
		compile(
				"A := [\"a\", \"b\", \"c\"]",
				"B := array (`string) (",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) b.value().getValueStruct();

		assertTrue(arraySruct.isVariable());
		assertThat(
				arraySruct.getItemTypeRef().getType(),
				is(b.getContext().getIntrinsics().getString()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertTrue(items[0].isVariable());
		assertTrue(items[1].isVariable());
		assertTrue(items[2].isVariable());
	}

}
