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
import static org.o42a.ast.ref.BodyRefNode.Suffix.BACKQUOTE;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BodyRefParserTest extends GrammarTestCase {

	@Test
	public void bodyRef() {

		final BodyRefNode bodyRef = to(
				BodyRefNode.class,
				parse("foo /*1*/ ` /*2*/"));

		assertName("foo", bodyRef.getOwner());
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));
		assertThat(this.worker.position().offset(), is(17L));
	}

	@Test
	public void bodyMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo` bar"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, memberRef.getOwner());

		assertName("foo", bodyRef.getOwner());
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(memberRef.getName().getName(), is("bar"));
		assertThat(memberRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedBodyMember() {

		final MemberRefNode memberRef =
				to(MemberRefNode.class, parse("Foo` bar @baz"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, memberRef.getOwner());

		assertName("foo", bodyRef.getOwner());
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertThat(memberRef.getName().getName(), is("bar"));
		assertName("baz", memberRef.getDeclaredIn());
	}

	@Test
	public void bodyAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo` @@bar"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, adapterRef.getOwner());

		assertName("foo", bodyRef.getOwner());
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertName("bar", adapterRef.getType());
		assertThat(adapterRef.getDeclaredIn(), nullValue());
	}

	@Test
	public void qualifiedBodyAdapter() {

		final AdapterRefNode adapterRef =
				to(AdapterRefNode.class, parse("Foo` @@bar @baz"));
		final BodyRefNode bodyRef =
				to(BodyRefNode.class, adapterRef.getOwner());

		assertName("foo", bodyRef.getOwner());
		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		assertName("bar", adapterRef.getType());
		assertName("baz", adapterRef.getDeclaredIn());
	}

	@Test
	public void memberBody() {

		final MemberRefNode ref =
				to(MemberRefNode.class, parse("foo: bar` baz"));

		assertThat(ref.getName().getName(), is("baz"));
		assertThat(ref.getDeclaredIn(), nullValue());

		final BodyRefNode bodyRef =
				to(BodyRefNode.class, ref.getOwner());

		assertThat(bodyRef.getSuffix().getType(), is(BACKQUOTE));

		final MemberRefNode memberRef =
				to(MemberRefNode.class, bodyRef.getOwner());

		assertName("foo", memberRef.getOwner());
		assertThat(memberRef.getName().getName(), is("bar"));
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
