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
package org.o42a.compiler.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.junit.Test;
import org.o42a.util.string.ID;


public class IDTest {

	@Test
	public void decaptialization() {

		final ID id =
				caseInsensitiveName("Abc").toID()
				.sub(caseInsensitiveName("Def"))
				.sub(caseInsensitiveName("GHI"));

		assertThat(id.toString(), is("Abc: def: GHI"));
	}

}
