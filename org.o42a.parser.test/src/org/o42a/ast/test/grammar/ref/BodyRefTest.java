/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.ref.BodyRefNode.Suffix.BACKQUOTE;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BodyRefTest extends GrammarTestCase {

	@Test
	public void bodyRef() {

		final BodyRefNode bodyRef = to(
				BodyRefNode.class,
				parse("foo ~~1~~ ` ~~2"));

		assertThat(bodyRef.getOwner(), isName("foo"));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));
		assertThat(this.worker.position().offset(), is(15L));
	}

	@Test
	public void bodyMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo` bar"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, memberRef.getOwner());

		assertThat(bodyRef.getOwner(), isName("foo"));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedBodyMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo` bar @baz"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, memberRef.getOwner());

		assertThat(bodyRef.getOwner(), isName("foo"));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void bodyAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo` @@bar"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, adapterRef.getOwner());

		assertThat(bodyRef.getOwner(), isName("foo"));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedBodyAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo` @@bar @baz"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, adapterRef.getOwner());

		assertThat(bodyRef.getOwner(), isName("foo"));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void memberBody() {

		final MemberRefNode ref =
				to(MemberRefNode.class, parse("foo: bar` baz"));

		assertThat(canonicalName(ref.getName()), is("baz"));
		assertThat(ref.getDeclaredIn(), nullValue());

		final BodyRefNode bodyRef =
				to(BodyRefNode.class, ref.getOwner());

		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		final MemberRefNode memberRef =
				to(MemberRefNode.class, bodyRef.getOwner());

		assertThat(memberRef.getOwner(), isName("foo"));
		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void scopeBody() {

		final BodyRefNode bodyRef = to(BodyRefNode.class, parse("*`"));
		final ScopeRefNode scopeRef =
				to(ScopeRefNode.class, bodyRef.getOwner());

		assertThat(scopeRef.getType(), is(ScopeType.IMPLIED));
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));
	}

	private final RefNode parse(String text) {
		return parse(ref(), text);
	}

}
