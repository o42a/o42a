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
package org.o42a.compiler.test.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Member;
import org.o42a.core.value.TypeParameter;


public class TypeDefinitionTest extends CompilerTestCase {

	@Test
	public void typeDefinition() {
		compile("A := void #(T := void)");

		final Member t = member("a", "t");

		assertThat(t.isTypeParameter(), is(true));

		final TypeParameter typeParameter =
				t.toTypeParameter().getTypeParameter();

		assertThat(
				typeParameter.getTypeRef().getType(),
				is(this.context.getVoid()));
	}

	@Test
	public void overrideTypeDefinition() {
		compile(
				"A := integer #(T := void) (= 1)",
				"B := a #(T = integer)");

		final Member bt = member("b", "t");

		assertThat(bt.isTypeParameter(), is(true));

		final TypeParameter typeParameter =
				bt.toTypeParameter().getTypeParameter();

		assertThat(
				typeParameter.getTypeRef().getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

	@Test
	public void overrideTypeParameter() {
		compile(
				"A := integer #(T := void) (= 1)",
				"B := a (`integer)");

		final Member bt = member("b", "t");

		assertThat(bt.isTypeParameter(), is(true));

		final TypeParameter typeParameter =
				bt.toTypeParameter().getTypeParameter();

		assertThat(
				typeParameter.getTypeRef().getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

}
