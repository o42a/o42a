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

import org.junit.Test;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class ScopeFieldTest extends GrammarTestCase {

	@Test
	public void moduleFieldRef() {

		final MemberRefNode ref = parse("$foo");

		assertEquals(
				ScopeType.MODULE,
				to(ScopeRefNode.class, ref.getOwner()).getType());
		assertEquals("foo", ref.getName().getName());
	}

	@Test
	public void rootFieldRef() {

		final MemberRefNode ref = parse("$$foo");

		assertEquals(
				ScopeType.ROOT,
				to(ScopeRefNode.class, ref.getOwner()).getType());
		assertEquals("foo", ref.getName().getName());
	}

	@Test
	public void fieldFieldRef() {

		final MemberRefNode ref = parse("::foo");

		assertEquals(
				ScopeType.PARENT,
				to(ScopeRefNode.class, ref.getOwner()).getType());
		assertEquals("foo", ref.getName().getName());
	}

	@Test
	public void impliedFieldRef() {

		final MemberRefNode ref = parse("*foo");

		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, ref.getOwner()).getType());
		assertEquals("foo", ref.getName().getName());
	}

	@Test
	public void selfFieldRef() {

		final MemberRefNode ref = parse(":foo");

		assertEquals(
				ScopeType.SELF,
				to(ScopeRefNode.class, ref.getOwner()).getType());
		assertEquals("foo", ref.getName().getName());
	}

	private MemberRefNode parse(String text) {
		return to(MemberRefNode.class, parse(Grammar.ref(), text));
	}

}
