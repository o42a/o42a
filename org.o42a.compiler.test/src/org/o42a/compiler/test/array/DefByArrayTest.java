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
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


public class DefByArrayTest extends CompilerTestCase {

	@Test
	public void buildRow() {
		compile(
				"A := [\"a\", \"b\", \"c\"]",
				"B := string` row (",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueType arrayType =
				b.type().getValueType().toArrayType();

		assertFalse(arrayType.isVariable());
		assertThat(
				arrayType.itemTypeRef(b.type().getParameters()).getType(),
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
				"B := string` array (",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueType arrayType =
				b.type().getValueType().toArrayType();

		assertTrue(arrayType.isVariable());
		assertThat(
				arrayType.itemTypeRef(b.type().getParameters()).getType(),
				is(b.getContext().getIntrinsics().getString()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertTrue(items[0].isVariable());
		assertTrue(items[1].isVariable());
		assertTrue(items[2].isVariable());
	}

}
