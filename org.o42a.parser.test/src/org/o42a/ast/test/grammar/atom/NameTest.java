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
package org.o42a.ast.test.grammar.atom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;


public class NameTest extends GrammarTestCase {

	private Parser<NameNode> parser;

	@Before
	public void setup() {
		this.parser = Grammar.name();
	}

	@Test
	public void simpleName() {

		final NameNode name = parse("abc");

		assertNotNull(name);
		assertRange(0, 3, name);
		assertEquals("abc", name.getName());
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void whitespaceSeparatedName() {

		final NameNode name = parse("a c");

		assertNotNull(name);
		assertRange(0, 3, name);
		assertEquals("a_c", name.getName());
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void number() {

		final NameNode name = parse("a3b");

		assertNotNull(name);
		assertRange(0, 3, name);
		assertEquals("a3b", name.getName());
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void numberAfterNumber() {

		final NameNode name = parse("a3 4b");

		assertNotNull(name);
		assertRange(0, 5, name);
		assertEquals("a3_4b", name.getName());
		assertEquals(5, this.worker.position().offset());
	}

	@Test
	public void startFromNumber() {

		final NameNode name = parse("3a");

		assertNull(name);
		assertEquals(0, this.worker.position().offset());
	}

	@Test
	public void startFromWhitespace() {

		final NameNode name = parse(" abc ");

		assertNotNull(name);
		assertRange(1, 4, name);
		assertEquals("abc", name.getName());
		assertEquals(4, this.worker.position().offset());
	}

	@Test
	public void hyphen() {

		final NameNode name = parse("a-b");

		assertNotNull(name);
		assertRange(0, 3, name);
		assertEquals("a-b", name.getName());
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void whitespaceAfterHyphen() {

		final NameNode name = parse("a- b");

		assertNotNull(name);
		assertRange(0, 4, name);
		assertEquals("a-b", name.getName());
		assertEquals(4, this.worker.position().offset());
	}

	@Test
	public void hyphenFirst() {

		final NameNode name = parse("-a");

		assertNull(name);
	}

	@Test
	public void hyphenLast() {

		final NameNode name = parse("a-");

		assertNotNull(name);
		assertRange(0, 1, name);
		assertEquals("a", name.getName());
		assertEquals(1, this.worker.position().offset());
	}

	@Test
	public void hyphenAfterWhitespace() {

		final NameNode name = parse("a -");

		assertNotNull(name);
		assertRange(0, 1, name);
		assertEquals("a", name.getName());
		assertEquals(1, this.worker.position().offset());
	}

	@Test
	public void hyphenAfterHyphen() {

		final NameNode name = parse("a--b");

		assertNotNull(name);
		assertRange(0, 1, name);
		assertEquals("a", name.getName());
		assertEquals(1, this.worker.position().offset());
	}

	private NameNode parse(String text) {
		return parse(this.parser, text);
	}

}
