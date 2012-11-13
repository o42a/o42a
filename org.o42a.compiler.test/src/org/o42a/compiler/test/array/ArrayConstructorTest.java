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
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


public class ArrayConstructorTest extends CompilerTestCase {

	@Test
	public void qualifiedRow() {
		compile("A := row (`integer) [[1, 2, 3]]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.value().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(arrayType.cast(a.type().getParameters()));

		assertFalse(arrayType.isVariable());
		assertThat(
				itemTypeRef.getType(),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(a);
		final ArrayItem[] items = array.items(a.getScope());

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
		compile("A := [1, 2, 3]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.value().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(arrayType.cast(a.type().getParameters()));

		assertFalse(arrayType.isVariable());
		assertThat(
				itemTypeRef.getType(),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(a);
		final ArrayItem[] items = array.items(a.getScope());

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
		compile("A := array (`integer) [[1, 2, 3]]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.value().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(arrayType.cast(a.type().getParameters()));

		assertTrue(arrayType.isVariable());
		assertThat(
				itemTypeRef.getType(),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(a);
		final ArrayItem[] items = array.items(a.getScope());

		assertThat(items.length, is(3));
		assertTrue(items[0].isVariable());
		assertTrue(items[1].isVariable());
		assertTrue(items[2].isVariable());
	}

	@Test
	public void unqualifiedArray() {
		compile("A := array [[1, 2, 3]]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.value().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(arrayType.cast(a.type().getParameters()));

		assertTrue(arrayType.isVariable());
		assertThat(
				itemTypeRef.getType(),
				is(a.getContext().getIntrinsics().getVoid()));

		final Array array = definiteValue(a);
		final ArrayItem[] items = array.items(a.getScope());

		assertThat(items.length, is(3));
		assertTrue(items[0].isVariable());
		assertTrue(items[1].isVariable());
		assertTrue(items[2].isVariable());
	}

}
