/*
    Parser Tests
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class MemberRefTest extends GrammarTestCase {

	@Test
	public void memberRef() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("foo"));

		assertName("foo", ref);
		assertEquals(3, this.worker.position().offset());
		assertRange(0, 3, ref);
	}

	@Test
	public void qualified() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("foo:bar"));
		final MemberRefNode owner = to(MemberRefNode.class, ref.getOwner());

		assertEquals("bar", ref.getName().getName());
		assertEquals(Qualifier.MEMBER_NAME, ref.getQualifier().getType());
		assertName("foo", owner);
		assertEquals(7, this.worker.position().offset());
		assertRange(0, 7, ref);
		assertRange(3, 4, ref.getQualifier());
		assertRange(4, 7, ref.getName());
		assertRange(0, 3, owner);
	}

	@Test
	public void declaredIn() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("foo@bar"));
		final MemberRefNode declaredIn = to(MemberRefNode.class, ref.getDeclaredIn());

		assertEquals("foo", ref.getName().getName());
		assertNull(ref.getQualifier());
		assertName("bar", declaredIn);
		assertEquals(7, this.worker.position().offset());
		assertRange(0, 7, ref);
		assertRange(0, 3, ref.getName());
		assertRange(3, 4, ref.getRetention());
		assertRange(4, 7, declaredIn.getName());
	}

	private RefNode parse(String text) {
		return parse(Grammar.ref(), text);
	}

}
