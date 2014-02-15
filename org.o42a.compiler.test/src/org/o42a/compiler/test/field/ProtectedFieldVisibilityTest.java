/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Accessor;
import org.o42a.core.value.ValueType;


public class ProtectedFieldVisibilityTest extends CompilerTestCase {

	@Test
	public void byNameInSameSource() {
		compile(
				"::A := 1",
				"B := a");

		assertThat(
				definiteValue(field("a", Accessor.OWNER), ValueType.INTEGER),
				is(1L));
		assertThat(definiteValue(field("b"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void inSameSource() {
		compile(
				"A := void (::foo := 3)",
				"B := void (bar := a: foo)");

		assertThat(definiteValue(field("b", "bar"), ValueType.INTEGER), is(3L));
	}

	@Test
	public void ownerByName() {
		addSource(
				"a",
				"::A := 2",
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
	public void enclosingByName() {
		addSource(
				"a",
				"::A := 1",
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
	public void fieldOfOwnerByName() {
		addSource(
				"a",
				"A := void",
				"=========");
		addSource(
				"a/b",
				"::B := 33",
				"========");
		addSource(
				"a/c",
				"::C := b",
				"=======");
		compile("");

		assertThat(
				definiteValue(
						field(field("a"), "c", Accessor.OWNER),
						ValueType.INTEGER),
				is(33L));
	}

	@Test
	public void derive() {
		compile(
				"A := void (",
				"  ::Foo := 1",
				")",
				"B := &a (",
				"  Bar := foo",
				")");

		assertThat(definiteValue(field("b", "bar"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void override() {
		compile(
				"A := void (",
				"  ::Foo := 1",
				")",
				"B := a (",
				"  foo = 2",
				")");

		assertThat(
				definiteValue(
						field(field("b"), "foo", Accessor.INHERITANT),
						ValueType.INTEGER),
				is(2L));
	}

}
