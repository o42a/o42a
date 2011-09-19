/*
    Parser Tests
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.field.DefinitionKind;
import org.o42a.ast.field.InterfaceNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ArrayTest extends GrammarTestCase {

	@Test
	public void immutableArray() {

		final BracketsNode result = parse("[`a, b, c]");
		final ArgumentNode[] arguments = result.getArguments();
		final InterfaceNode iface = result.getInterface();

		assertNull(iface.getOpening());
		assertThat(iface.getKind().getType(), is(DefinitionKind.LINK));
		assertNull(iface.getType());
		assertNull(iface.getClosing());
		assertThat(arguments.length, is(3));
		assertName("a", arguments[0].getValue());
		assertName("b", arguments[1].getValue());
		assertName("c", arguments[2].getValue());
	}

	@Test
	public void mutableArray() {

		final BracketsNode result = parse("[``a, b, c]");
		final ArgumentNode[] arguments = result.getArguments();
		final InterfaceNode iface = result.getInterface();

		assertNull(iface.getOpening());
		assertThat(iface.getKind().getType(), is(DefinitionKind.VARIABLE));
		assertNull(iface.getType());
		assertNull(iface.getClosing());
		assertThat(arguments.length, is(3));
		assertName("a", arguments[0].getValue());
		assertName("b", arguments[1].getValue());
		assertName("c", arguments[2].getValue());
	}

	@Test
	public void immutableItemType() {

		final BracketsNode result = parse("[(`foo) a, b, c]");
		final ArgumentNode[] arguments = result.getArguments();
		final InterfaceNode iface = result.getInterface();

		assertNotNull(iface.getOpening());
		assertThat(iface.getKind().getType(), is(DefinitionKind.LINK));
		assertName("foo", iface.getType());
		assertNotNull(iface.getClosing());
		assertThat(arguments.length, is(3));
		assertName("a", arguments[0].getValue());
		assertName("b", arguments[1].getValue());
		assertName("c", arguments[2].getValue());
	}

	@Test
	public void mutableItemType() {

		final BracketsNode result = parse("[(``foo) a, b, c]");
		final ArgumentNode[] arguments = result.getArguments();
		final InterfaceNode iface = result.getInterface();

		assertNotNull(iface.getOpening());
		assertThat(iface.getKind().getType(), is(DefinitionKind.VARIABLE));
		assertName("foo", iface.getType());
		assertNotNull(iface.getClosing());
		assertThat(arguments.length, is(3));
		assertName("a", arguments[0].getValue());
		assertName("b", arguments[1].getValue());
		assertName("c", arguments[2].getValue());
	}

	private BracketsNode parse(String... lines) {

		final BracketsNode result = parseLines(DECLARATIVE.brackets(), lines);

		assertNotNull(result.getInterface());

		return result;
	}

}
