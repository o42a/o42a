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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class AscendantRefTest extends GrammarTestCase {

	@Test
	public void ascendant() {

		final AscendantRefNode result = parse("^");

		assertNull(result.getOverridden());
		assertNoType(result);
	}

	@Test
	public void ascendantWithType() {

		final AscendantRefNode result = parse("^foo^");

		assertNull(result.getOverridden());
		assertWithType("foo", result);
	}

	@Test
	public void scopeAscendant() {
		assertScopeAscendant(ScopeType.SELF, ":^");
		assertScopeAscendant(ScopeType.PARENT, "::^");
		assertScopeAscendant(ScopeType.MODULE, "$^");
		assertScopeAscendant(ScopeType.ROOT, "$$^");
	}

	@Test
	public void scopeAscendantWithType() {
		assertScopeAscendant(ScopeType.SELF, "foo", ":^foo^");
		assertScopeAscendant(ScopeType.PARENT, "foo", "::^foo^");
		assertScopeAscendant(ScopeType.MODULE, "foo", "$^foo^");
		assertScopeAscendant(ScopeType.ROOT, "foo", "$$^foo^");
	}

	@Test
	public void parentAscendant() {

		final AscendantRefNode result = parse("foo::^");

		assertNoType(result);

		final ParentRefNode overridden =
				to(ParentRefNode.class, result.getOverridden());

		assertThat(overridden.getName().getName(), is("foo"));
	}

	@Test
	public void parentAscendantWithType() {

		final AscendantRefNode result = parse("foo::^bar^");

		assertWithType("bar", result);

		final ParentRefNode overridden =
				to(ParentRefNode.class, result.getOverridden());

		assertThat(overridden.getName().getName(), is("foo"));
	}

	private void assertScopeAscendant(ScopeType scopeType, String text) {

		final AscendantRefNode result = parse(text);

		assertThat(
				to(ScopeRefNode.class, result.getOverridden()).getType(),
				is(scopeType));
		assertNoType(result);
	}

	private void assertScopeAscendant(
			ScopeType scopeType,
			String typeName,
			String text) {

		final AscendantRefNode result = parse(text);

		assertThat(
				to(ScopeRefNode.class, result.getOverridden()).getType(),
				is(scopeType));
		assertWithType(typeName, result);
	}

	private void assertNoType(AscendantRefNode result) {
		assertThat(
				result.getPrefix().getType(),
				is(AscendantRefNode.Boundary.CIRCUMFLEX));
		assertNull(result.getType());
		assertNull(result.getSuffix());
	}

	private void assertWithType(String typeName, AscendantRefNode result) {
		assertThat(
				result.getPrefix().getType(),
				is(AscendantRefNode.Boundary.CIRCUMFLEX));
		assertName(typeName, result.getType());
		assertThat(
				result.getSuffix().getType(),
				is(AscendantRefNode.Boundary.CIRCUMFLEX));
	}

	private AscendantRefNode parse(String text) {
		return to(AscendantRefNode.class, parse(Grammar.ref(), text));
	}

}
