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
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.value.ValueType;


public class DefByArrayTest extends CompilerTestCase {

	@Test
	public void buildConstantArray() {
		compile(
				"A := [`\"a\", \"b\", \"c\"]",
				"B := constant array & [string](",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) b.value().getValueStruct();

		assertTrue(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(b.getContext().getIntrinsics().getString()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertThat(
				definiteValue(items[0].getArtifact(), ValueType.STRING),
				is("a"));
		assertThat(
				definiteValue(items[1].getArtifact(), ValueType.STRING),
				is("b"));
		assertThat(
				definiteValue(items[2].getArtifact(), ValueType.STRING),
				is("c"));
	}

	@Test
	public void buildVariableArray() {
		compile(
				"A := [`\"a\", \"b\", \"c\"]",
				"B := variable array & [string](",
				"  = A",
				")");

		final Obj b = field("b").toObject();

		final ArrayValueStruct arraySruct =
				(ArrayValueStruct) b.value().getValueStruct();

		assertFalse(arraySruct.isConstant());
		assertThat(
				arraySruct.getItemTypeRef().typeObject(dummyUser()),
				is(b.getContext().getIntrinsics().getString()));

		final Array array = definiteValue(b);
		final ArrayItem[] items = array.items(b.getScope());

		assertThat(items.length, is(3));
		assertTrue(items[0].getArtifact().toLink().isVariable());
		assertTrue(items[1].getArtifact().toLink().isVariable());
		assertTrue(items[2].getArtifact().toLink().isVariable());
	}

}
