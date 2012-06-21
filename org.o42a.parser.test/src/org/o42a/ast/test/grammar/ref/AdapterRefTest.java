/*
    Parser Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.AdapterRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class AdapterRefTest extends GrammarTestCase {

	@Test
	public void adapterRef() {

		final AdapterRefNode ref = parse("foo@@bar");

		assertName("foo", ref.getOwner());
		assertRange(3, 5, ref.getAdapterId().getPrefix());
		assertName("bar", ref.getType());
		assertThat(ref.getMembership(), nullValue());
	}

	@Test
	public void qualifiedAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@baz");

		assertName("foo", ref.getOwner());
		assertRange(3, 5, ref.getAdapterId().getPrefix());

		final MemberRefNode type = to(MemberRefNode.class, ref.getType());

		assertName("bar", type);

		assertRange(8, 9, ref.getMembership().getPrefix());
		assertName("baz", ref.getDeclaredIn());
	}

	@Test
	public void doubleAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@@baz@type");
		final AdapterRefNode owner = to(AdapterRefNode.class, ref.getOwner());

		assertName("foo", owner.getOwner());
		assertRange(3, 5, owner.getAdapterId().getPrefix());

		assertName("bar", owner.getType());

		assertRange(8, 10, ref.getAdapterId().getPrefix());
		assertThat(
				canonicalName(
						to(MemberRefNode.class, ref.getType()).getName()),
				is("baz"));
		assertRange(13, 14, ref.getMembership().getPrefix());
		assertName("type", ref.getDeclaredIn());
	}

	@Test
	public void parentheses() {

		final AdapterRefNode ref = parse("foo@@(bar)@(type)");
		final MemberRefNode owner = to(MemberRefNode.class, ref.getOwner());

		assertThat(canonicalName(owner.getName()), is("foo"));
		assertName("bar", ref.getAdapterId().getType());
		assertName("type", ref.getDeclaredIn());
	}

	@Test
	public void qualifiedType() {

		final AdapterRefNode ref = parse("foo@@(bar@type)");
		final MemberRefNode type =
				to(MemberRefNode.class, ref.getAdapterId().getType());

		assertName("foo", ref.getOwner());
		assertThat(canonicalName(type.getName()), is("bar"));
		assertName("type", type.getDeclaredIn());
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlBeforeQualifier() {

		final RefNode ref = parse(ref(), "foo\n@@");

		assertName("foo", ref);
	}

	@Test
	public void nlAfterQualifier() {
		expectError("missing_type");

		final AdapterRefNode ref = parse("foo@@\nbar");

		assertName("foo", ref.getOwner());
		assertThat(ref.getType(), nullValue());
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlBeforeRetention() {

		final AdapterRefNode ref = parse("foo @@bar \n@baz");

		assertName("foo", ref.getOwner());
		assertName("bar", ref.getType());
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlAfterRetention() {
		expectError("missing_type");

		final AdapterRefNode ref = parse("foo @@bar @\nbaz");

		assertName("foo", ref.getOwner());
		assertName("bar", ref.getType());
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	private AdapterRefNode parse(String text) {
		return to(AdapterRefNode.class, parse(ref(), text));
	}

}
