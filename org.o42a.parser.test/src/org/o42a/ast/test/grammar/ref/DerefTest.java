/*
    Parser Tests
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
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.ref.DerefNode.Suffix.ARROW;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class DerefTest extends GrammarTestCase {

	@Test
	public void deref() {

		final DerefNode deref = to(
				DerefNode.class,
				parse("foo ~~1~~ -> ~~2~~"));

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(ARROW));
		assertThat(this.worker.position().offset(), is(18L));
	}

	@Test
	public void derefMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo -> bar"));
		final DerefNode deref =
				to(DerefNode.class, memberRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(ARROW));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedDerefMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo -> bar @baz"));
		final DerefNode deref =
				to(DerefNode.class, memberRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(ARROW));

		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void derefAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo -> @@bar"));
		final DerefNode deref =
				to(DerefNode.class, adapterRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(ARROW));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedDerefAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo -> @@bar @baz"));
		final DerefNode deref =
				to(DerefNode.class, adapterRef.getOwner());

		assertThat(deref.getOwner(), isName("foo"));
		assertThat(deref.getSuffix().getType(), is(ARROW));

		assertThat(adapterRef.getType(), isName("bar"));
		assertThat(adapterRef.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void memberDeref() {

		final MemberRefNode ref =
				to(MemberRefNode.class, parse("foo: bar -> baz"));

		assertThat(canonicalName(ref.getName()), is("baz"));
		assertThat(ref.getDeclaredIn(), nullValue());

		final DerefNode deref =
				to(DerefNode.class, ref.getOwner());

		assertThat(deref.getSuffix().getType(), is(ARROW));

		final MemberRefNode memberRef =
				to(MemberRefNode.class, deref.getOwner());

		assertThat(memberRef.getOwner(), isName("foo"));
		assertThat(canonicalName(memberRef.getName()), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void scopeDeref() {

		final DerefNode deref = to(DerefNode.class, parse("*->"));
		final ScopeRefNode scopeRef =
				to(ScopeRefNode.class, deref.getOwner());

		assertThat(scopeRef.getType(), is(ScopeType.IMPLIED));
		assertThat(deref.getSuffix().getType(), is(ARROW));
	}

	private final RefNode parse(String text) {
		return parse(ref(), text);
	}

}
