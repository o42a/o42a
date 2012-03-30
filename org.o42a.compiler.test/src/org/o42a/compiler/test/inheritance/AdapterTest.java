/*
    Compiler Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.compiler.test.inheritance;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;


public class AdapterTest extends CompilerTestCase {

	private Obj adapterType;
	private Obj a;

	@Test
	public void adapter() {
		compile(
				"Adapter :=> void(Foo := 1);",
				"a := void(@Adapter := adapter)");

		final Ref adapter = this.a.selfRef().adapt(
				this.a.getContext(),
				this.adapterType.selfRef().toStaticTypeRef());

		assertThat(adapter, notNullValue());
	}

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		this.adapterType = field("adapter").toObject();
		this.a = field("a").toObject();
	}

}
