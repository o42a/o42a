/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
				"B := integer` a");

		final Member bt = member("b", "t");

		assertThat(bt.isTypeParameter(), is(true));

		final TypeParameter typeParameter =
				bt.toTypeParameter().getTypeParameter();

		assertThat(
				typeParameter.getTypeRef().getType(),
				is(this.context.getIntrinsics().getInteger()));
	}

}
