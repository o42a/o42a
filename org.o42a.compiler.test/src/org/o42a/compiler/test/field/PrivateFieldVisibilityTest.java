/*
    Compiler Tests
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.test.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Accessor;
import org.o42a.core.value.ValueType;


public class PrivateFieldVisibilityTest extends CompilerTestCase {

	@Test
	public void visibleByNameInSameSource() {
		compile(
				":A := 1",
				"B := a");

		assertThat(
				definiteValue(field("a", Accessor.OWNER), ValueType.INTEGER),
				is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void visibleInSameSource() {
		compile(
				"A := void (:foo := 3)",
				"B := void (bar := a: foo)");

		assertThat(definiteValue(field("b", "bar"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void visibleByNameOwner() {
		addSource(
				"a",
				":A := 2",
				"=======");
		addSource(
				"a/b",
				"B := a",
				"======");
		compile("");

		assertThat(
				definiteValue(
						field(field("a", Accessor.OWNER), "b"),
						ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void visibleByNameEnclosing() {
		addSource(
				"a",
				":A := 1",
				"=======");
		addSource(
				"a/b",
				"B := void",
				"======");
		addSource(
				"a/b/c",
				"C := a",
				"======");
		compile("");

		assertThat(
				definiteValue(
						field(field("a", Accessor.OWNER), "b", "c"),
						ValueType.INTEGER),
				is(1L));
	}

	@Test
	public void visibleByNameFieldOfOwner() {
		addSource(
				"a",
				"A := void",
				"=========");
		addSource(
				"a/b",
				":B := 33",
				"========");
		addSource(
				"a/c",
				":C := b",
				"=======");
		compile("");

		assertThat(
				definiteValue(
						field(field("a"), "c", Accessor.OWNER),
						ValueType.INTEGER),
				is(33L));
	}

}
