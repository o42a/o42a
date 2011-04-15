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
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ReproductionTest extends CompilerTestCase {

	@Test
	public void referOutsideObject() {
		compile(
				"Container := void(",
				"  Referred := 2",
				")",
				"Object :=> integer(",
				"  <Refer> = Container: referred",
				")",
				"Result := object_refer");

		assertThat(definiteValue(field("result"), Long.class), is(2L));
	}

	@Test
	public void ancestorRefersOutsideObject() {
		compile(
				"Container := void(",
				"  Referred := 2",
				")",
				"Object :=> void(",
				"  Value :=< integer",
				"  <Refer> Value = container: referred",
				")",
				"Result := object_refer");

		assertThat(definiteValue(field("result", "value"), Long.class), is(2L));
	}

	@Test
	public void expressionRefersOutsideObject() {
		compile(
				"Container := void(",
				"  Referred := False",
				")",
				"Object :=> void(",
				"  <Refer> Container: referred",
				")",
				"Result := object_refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void nestedExpressionRefersOutsideObject() {
		compile(
				"Container := void(",
				"  Referred := False",
				")",
				"Object :=> void(",
				"  <Construct> Container(",
				"    <Refer> Referred",
				"  )",
				")",
				"Result := object_construct_refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void nestedAncestorRefersOutsideObject() {
		compile(
				"Container := void(",
				"  Referred := 2",
				")",
				"Object :=> void(",
				"  Foo := void(",
				"    Value := 1",
				"  )",
				"  <*> Foo = *(",
				"    <Refer> Value = container: referred",
				"  )",
				")",
				"Result := object_refer");

		assertThat(
				definiteValue(field("result", "foo", "value"), Long.class),
				is(2L));
	}

}
