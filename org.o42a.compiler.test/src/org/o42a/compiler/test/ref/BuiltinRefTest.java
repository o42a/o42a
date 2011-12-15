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


public class BuiltinRefTest extends CompilerTestCase {

	@Test
	public void voidRef() {
		compile("A := `void");

		assertThat(
				field("a").getArtifact().materialize().getWrapped(),
				is(this.context.getVoid()));
		assertTrueVoid(field("a"));
	}

	@Test
	public void rootVoidRef() {
		compile("A := `void");

		assertThat(
				field("a").getArtifact().materialize().getWrapped(),
				is(this.context.getVoid()));
		assertTrueVoid(field("a"));
	}

	@Test
	public void falseRef() {
		compile("A := `false");

		assertThat(
				field("a").getArtifact().materialize().getWrapped(),
				is(this.context.getFalse()));
		assertFalseVoid(field("a"));
	}

	@Test
	public void rootFalseRef() {
		compile("A := `$$false");

		assertThat(
				field("a").getArtifact().materialize().getWrapped(),
				is(this.context.getFalse()));
		assertFalseVoid(field("a"));
	}

}
