/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.array;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.array.ArrayValueType.ARRAY;
import static org.o42a.core.value.array.ArrayValueType.ROW;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;


public class ArrayLinkTest extends CompilerTestCase {

	@Test
	public void qualifiedRow() {
		compile("A := `integer` row [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final TypeParameters<Array> arrayParameters =
				ROW.cast(
						LINK.interfaceRef(a.type().getParameters())
						.getParameters());

		assertThat(arrayParameters.getValueType().isVariable(), is(false));
		assertThat(
				ROW.itemTypeRef(arrayParameters).getType(),
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
		final TypeParameters<Array> arrayParameters =
				ROW.cast(
						LINK.interfaceRef(a.type().getParameters())
						.getParameters());

		assertThat(arrayParameters.getValueType().isVariable(), is(false));
		assertThat(
				ROW.itemTypeRef(arrayParameters).getType(),
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
		compile("A := `integer` array [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final TypeParameters<Array> arrayParameters =
				ARRAY.cast(
						LINK.interfaceRef(a.type().getParameters())
						.getParameters());

		assertThat(arrayParameters.getValueType().isVariable(), is(true));
		assertThat(
				ARRAY.itemTypeRef(arrayParameters).getType(),
				is(a.getContext().getIntrinsics().getInteger()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

		assertThat(items.length, is(3));
		assertThat(items[0].isVariable(), is(true));
		assertThat(items[1].isVariable(), is(true));
		assertThat(items[2].isVariable(), is(true));
	}

	@Test
	public void unqualifiedArray() {
		compile("A := `array [[1, 2, 3]]");

		final Obj a = field("a").toObject();
		final TypeParameters<Array> arrayParameters =
				ARRAY.cast(
						LINK.interfaceRef(a.type().getParameters())
						.getParameters());

		assertThat(arrayParameters.getValueType().isVariable(), is(true));
		assertThat(
				ARRAY.itemTypeRef(arrayParameters).getType(),
				is(a.getContext().getIntrinsics().getVoid()));

		final Array array = definiteValue(linkTarget(a));
		final ArrayItem[] items = array.items(array.getScope());

		assertThat(items.length, is(3));
		assertThat(items[0].isVariable(), is(true));
		assertThat(items[1].isVariable(), is(true));
		assertThat(items[2].isVariable(), is(true));
	}

}
