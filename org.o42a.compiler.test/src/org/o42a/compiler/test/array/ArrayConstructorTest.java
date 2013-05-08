/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
		compile("A := integer` row [[1, 2, 3]]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.type().getValueType().toArrayType();
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
				a.type().getValueType().toArrayType();
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
		compile("A := integer` array [[1, 2, 3]]");

		final Obj a = field("a").toObject();

		final ArrayValueType arrayType =
				a.type().getValueType().toArrayType();
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
				a.type().getValueType().toArrayType();
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
