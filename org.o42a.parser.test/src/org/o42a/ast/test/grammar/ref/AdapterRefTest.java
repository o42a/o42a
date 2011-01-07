/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import org.o42a.ast.ref.AdapterRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class AdapterRefTest extends GrammarTestCase {

	@Test
	public void adapterRef() {

		final AdapterRefNode ref = parse("foo@@bar");

		assertName("foo", ref.getOwner());
		assertRange(3, 5, ref.getQualifier());
		assertName("bar", ref.getType());
		assertNull(ref.getRetention());
		assertNull(ref.getDeclaredIn());
	}

	@Test
	public void qualifiedAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@baz");

		assertName("foo", ref.getOwner());
		assertRange(3, 5, ref.getQualifier());

		final MemberRefNode type = to(MemberRefNode.class, ref.getType());

		assertName("bar", type);

		assertRange(8, 9, ref.getRetention());
		assertName("baz", ref.getDeclaredIn());
	}

	@Test
	public void doubleAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@@baz@type");
		final AdapterRefNode owner = to(AdapterRefNode.class, ref.getOwner());

		assertName("foo", owner.getOwner());
		assertRange(3, 5, owner.getQualifier());

		assertName("bar", owner.getType());

		assertRange(8, 10, ref.getQualifier());
		assertEquals(
				"baz",
				to(MemberRefNode.class, ref.getType()).getName().getName());
		assertRange(13, 14, ref.getRetention());
		assertName("type", ref.getDeclaredIn());
	}

	private AdapterRefNode parse(String text) {
		return to(AdapterRefNode.class, parse(Grammar.ref(), text));
	}

}
