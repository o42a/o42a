/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.ref.EagerRefNode.Suffix.CHEVRON;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class EagerRefTest extends GrammarTestCase {

	@Test
	public void eagerRef() {

		final EagerRefNode deref = to(
				EagerRefNode.class,
				parse("foo ~~1~~ >> ~~2~~"));

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));
		assertThat(this.worker.position().offset(), is(18L));
	}

	@Test
	public void eagerRefMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo>>bar"));
		final EagerRefNode deref =
				to(EagerRefNode.class, memberRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedEagerRefMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo>>bar @baz"));
		final EagerRefNode deref =
				to(EagerRefNode.class, memberRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void eagerRefAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo>>@@bar"));
		final EagerRefNode deref =
				to(EagerRefNode.class, adapterRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedEagerRefAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo>>@@bar @baz"));
		final EagerRefNode deref =
				to(EagerRefNode.class, adapterRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void memberEagerRef() {

		final MemberRefNode ref =
				to(MemberRefNode.class, parse("foo: bar>>baz"));

		assertThat(canonicalName(ref.getName()), is("baz"));
		assertThat(ref.getDeclaredIn(), nullValue());

		final EagerRefNode deref =
				to(EagerRefNode.class, ref.getOwner());

		assertThat(deref.getSuffix().getType(), is(CHEVRON));

		final MemberRefNode memberRef =
				to(MemberRefNode.class, deref.getOwner());

		assertThat(memberRef.getOwner(), isName("foo"));
		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void scopeEagerRef() {

		final EagerRefNode deref = to(EagerRefNode.class, parse("*>>"));
		final ScopeRefNode scopeRef =
				to(ScopeRefNode.class, deref.getOwner());

		assertThat(scopeRef.getType(), is(ScopeType.IMPLIED));
		assertThat(deref.getSuffix().getType(), is(CHEVRON));
	}

	private final RefNode parse(String text) {
		return parse(ref(), text);
	}

}
