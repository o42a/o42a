/*
    Compiler Tests
    Copyright (C) 2011 Ruslan Lopatin

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
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class ArrayConstructorTest extends CompilerTestCase {

	@Test
	public void qualifiedConstantArray() {
		compile("A := [(`integer) 1, 2, 3]");

		final Field<?> a = field("a");
		final Array array = definiteValue(a);
		final ArrayItem[] items = array.items(a);

		assertThat(items.length, is(3));
		assertThat(
				definiteValue(items[0].getArtifact(), ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(items[1].getArtifact(), ValueType.INTEGER),
				is(2L));
		assertThat(
				definiteValue(items[2].getArtifact(), ValueType.INTEGER),
				is(3L));
	}

}
