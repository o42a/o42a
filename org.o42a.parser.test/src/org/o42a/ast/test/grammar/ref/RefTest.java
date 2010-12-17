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
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class RefTest extends GrammarTestCase {

	@Test
	public void parentScopedRef() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("::foo"));
		final ScopeRefNode owner = to(ScopeRefNode.class, ref.getOwner());

		assertEquals("foo", ref.getName().getName());
		assertNull(ref.getQualifier());
		assertNull(ref.getDeclaredIn());
		assertEquals(5, this.worker.position().offset());
		assertRange(0, 5, ref);
		assertNull(ref.getQualifier());
		assertEquals(ScopeType.PARENT, owner.getType());
		assertRange(0, 2, owner);
	}

	@Test
	public void parentRef() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("foo::bar"));
		final ParentRefNode owner = to(ParentRefNode.class, ref.getOwner());

		assertEquals("bar", ref.getName().getName());
		assertNull(ref.getQualifier());
		assertNull(ref.getDeclaredIn());
		assertEquals(8, this.worker.position().offset());
		assertRange(0, 8, ref);
		assertNull(ref.getQualifier());
		assertEquals("foo", owner.getName().getName());
		assertRange(0, 5, owner);
		assertRange(3, 5, owner.getQualifier());
	}

	private RefNode parse(String text) {
		return parse(Grammar.ref(), text);
	}

}
