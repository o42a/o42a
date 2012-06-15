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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.parentRef;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ParentRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ParentRefTest extends GrammarTestCase {

	@Test
	public void parent() {

		final ParentRefNode ref = parse("foo :: /* */");

		assertThat(ref, notNullValue());
		assertRange(0, 6, ref);
		assertThat(canonicalName(ref.getName()), is("foo"));
		assertRange(0, 3, ref.getName());
		assertRange(4, 6, ref.getQualifier());
	}

	@Test
	public void parentMember() {

		final MemberRefNode result =
				to(MemberRefNode.class, parse(ref(), "foo::bar"));

		assertThat(canonicalName(result.getName()), is("bar"));

		final ParentRefNode owner = to(ParentRefNode.class, result.getOwner());

		assertThat(canonicalName(owner.getName()), is("foo"));
	}

	private ParentRefNode parse(String text) {
		return parse(parentRef(), text);
	}

}
