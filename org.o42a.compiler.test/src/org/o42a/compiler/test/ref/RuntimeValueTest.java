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
package org.o42a.compiler.test.ref;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class RuntimeValueTest extends CompilerTestCase {

	@Test
	public void runtimeInteger() {
		compile(
				"Use namespace 'Test'",
				"A := rt-integer '5'",
				"B := a");

		assertRuntimeInteger(field("a").toObject());
		assertRuntimeInteger(field("b").toObject());
	}

	@Test
	public void runtimeIntegerLink() {
		compile(
				"Use namespace 'Test'",
				"A := (`integer) rt-integer '5'",
				"B := (`integer) a");

		assertRuntimeInteger(linkTarget(field("a")));
		assertRuntimeInteger(linkTarget(field("b")));
	}

	@Test
	public void imperativeRuntimeInteger() {
		compile(
				"Use namespace 'Test'",
				"A := integer({= Rt-integer '5'})",
				"B := a");

		assertRuntimeInteger(field("a").toObject());
		assertRuntimeInteger(field("b").toObject());
	}

	private static void assertRuntimeInteger(Obj object) {
		assertEquals(ValueType.INTEGER, object.value().getValueType());
		assertRuntimeValue(object.value().getValue());
	}

}
