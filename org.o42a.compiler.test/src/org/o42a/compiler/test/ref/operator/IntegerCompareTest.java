/*
    Compiler Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.test.ref.operator;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.artifact.object.Obj;
import org.o42a.util.Source;


public class IntegerCompareTest extends CompilerTestCase {

	private Obj result;

	@Test
	public void less() {
		assertTrueVoid(compare("Result := 1 < 2"));
		assertFalseVoid(compare("Result := 2 < 1"));
	}

	@Test
	public void lessOrEqual() {
		assertTrueVoid(compare("Result := 1 <= 2"));
		assertFalseVoid(compare("Result := 2 <= 1"));
	}

	@Test
	public void greater() {
		assertTrueVoid(compare("Result := 2 > 1"));
		assertFalseVoid(compare("Result := 1 > 2"));
	}

	@Test
	public void greaterOrEqual() {
		assertTrueVoid(compare("Result := 2 >= 1"));
		assertFalseVoid(compare("Result := 1 >= 2"));
	}

	@Test
	public void equal() {
		assertTrueVoid(compare("Result := 2 == 2"));
		assertFalseVoid(compare("Result := 1 == 2"));
	}

	@Test
	public void notEqual() {
		assertTrueVoid(compare("Result := 1 <> 2"));
		assertFalseVoid(compare("Result := 1 <> 1"));
	}

	private Obj compare(String line, String... lines) {
		compile(line, lines);
		return this.result;
	}

	@Override
	protected void compile(Source source) {
		super.compile(source);
		this.result = field("result").getArtifact().materialize();
	}

}
