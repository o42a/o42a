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
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class NamespaceTest extends CompilerTestCase {

	@Test
	public void objectAlias() {
		compile(
				"Use object 'used' as 'a'",
				"Used := 1",
				"User := a");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void nestedObjectAlias() {
		compile(
				"Use object 'ns: used' as 'a'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := a");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void useObject() {
		compile(
				"Use object 'ns: used'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := used");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void sameNamespace() {
		compile(
				"Use namespace _ object 'ns'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := used");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

}
