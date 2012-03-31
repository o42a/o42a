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
import static org.o42a.analysis.use.User.dummyUser;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayItem;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.value.ValueType;


public class ArrayLinkTest extends CompilerTestCase {

	@Test
	public void qualifiedRow() {
		compile("A := `row (`integer) [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) a.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getValueStruct();

		assertTrue(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

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
	public void unqualifiedRow() {
		compile("A := `[1, 2, 3]");

		final Obj a = field("a").toObject();
		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) a.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getValueStruct();

		assertTrue(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

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
	public void qualifiedArray() {
		compile("A := `array (`integer) [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) a.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getValueStruct();

		assertFalse(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

		assertThat(items.length, is(3));
		assertFalse(items[0].isConstant());
		assertFalse(items[1].isConstant());
		assertFalse(items[2].isConstant());
	}

	@Test
	public void unqualifiedArray() {
		compile("A := `array [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) a.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getValueStruct();

		assertFalse(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(a.getContext().getIntrinsics().getVoid()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

		assertThat(items.length, is(3));
		assertFalse(items[0].isConstant());
		assertFalse(items[1].isConstant());
		assertFalse(items[2].isConstant());
	}

}
