/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


public class ArrayDerivationTest extends CompilerTestCase {

	@Test
	public void inherit() {
		compile(
				"A := [1, 2, 3]",
				"B := a");

		final Obj b = field("b").toObject();

		final ArrayValueType arrayType =
				b.type().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(arrayType.cast(b.type().getParameters()));

		assertThat(arrayType.isVariable(), is(false));
		assertThat(
				itemTypeRef.getType(),
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
				"A := void (",
				"  Field := [1, 2, 3]",
				")",
				"B := a");

		final Obj bField = field("b", "field").toObject();

		final ArrayValueType arrayType =
				bField.type().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(
						arrayType.cast(bField.type().getParameters()));

		assertThat(arrayType.isVariable(), is(false));
		assertThat(
				itemTypeRef.getType(),
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
				"A := void (",
				"  F := `1",
				"  G := `2",
				"  Field := integer` row [[f + g]]",
				")",
				"B := a (F = 2. G = 4)");

		final Obj bField = field("b", "field").toObject();

		final ArrayValueType arrayType =
				bField.type().getValueType().toArrayType();
		final TypeRef itemTypeRef =
				arrayType.itemTypeRef(
						arrayType.cast(bField.type().getParameters()));

		assertThat(arrayType.isVariable(), is(false));
		assertThat(
				itemTypeRef.getType(),
				is(bField.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(bField);
		final ArrayItem[] items = array.items(bField.getScope());

		assertThat(items.length, is(1));
		assertThat(
				definiteValue(items[0].getTarget(), ValueType.INTEGER),
				is(6L));
	}

}
