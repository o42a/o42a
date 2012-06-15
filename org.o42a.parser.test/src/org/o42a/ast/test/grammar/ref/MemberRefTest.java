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
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class MemberRefTest extends GrammarTestCase {

	@Test
	public void memberRef() {

		final MemberRefNode ref = parse("foo");

		assertName("foo", ref);
		assertThat(this.worker.position().offset(), is(3L));
		assertRange(0, 3, ref);
	}

	@Test
	public void qualified() {

		final MemberRefNode ref = parse("foo:bar");
		final MemberRefNode owner = to(MemberRefNode.class, ref.getOwner());

		assertThat(canonicalName(ref.getName()), is("bar"));
		assertThat(ref.getQualifier().getType(), is(Qualifier.MEMBER_NAME));
		assertName("foo", owner);
		assertThat(this.worker.position().offset(), is(7L));
		assertRange(0, 7, ref);
		assertRange(3, 4, ref.getQualifier());
		assertRange(4, 7, ref.getName());
		assertRange(0, 3, owner);
	}

	@Test
	public void declaredIn() {

		final MemberRefNode ref = parse("foo@bar");
		final MemberRefNode declaredIn =
				to(MemberRefNode.class, ref.getDeclaredIn());

		assertThat(canonicalName(ref.getName()), is("foo"));
		assertThat(ref.getQualifier(), nullValue());
		assertName("bar", declaredIn);
		assertThat(this.worker.position().offset(), is(7L));
		assertRange(0, 7, ref);
		assertRange(0, 3, ref.getName());
		assertRange(3, 4, ref.getRetention());
		assertRange(4, 7, declaredIn.getName());
	}

	@Test
	public void nlBeforeQualifier() {

		final RefNode ref = parse(ref(), "foo \n: bar");

		assertName("foo", ref);
	}

	@Test
	public void nlAfterQualifier() {

		final MemberRefNode ref = parse("foo:\nbar");

		assertName("foo", ref);
	}

	@Test
	public void nlBeforeRetention() {

		final MemberRefNode ref = parse("foo: bar\n@ baz");

		assertName("foo", ref.getOwner());
		assertThat(canonicalName(ref.getName()), is("bar"));
		assertThat(ref.getRetention(), nullValue());
	}

	@Test
	public void nlAfterRetention() {
		expectError("missing_declared_in");

		final MemberRefNode ref = parse("foo: bar @\nbaz");

		assertName("foo", ref.getOwner());
		assertThat(canonicalName(ref.getName()), is("bar"));
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	private MemberRefNode parse(String text) {
		return to(MemberRefNode.class, parse(ref(), text));
	}

}
