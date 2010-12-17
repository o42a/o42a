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
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class ScopeRefTest extends GrammarTestCase {

	@Test
	public void self() {

		final ScopeRefNode ref = parse(": ");

		assertEquals(ScopeType.SELF, ref.getType());
		assertRange(0, 1, ref);
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void selfEOF() {

		final ScopeRefNode ref = parse(":");

		assertEquals(ScopeType.SELF, ref.getType());
		assertRange(0, 1, ref);
	}

	@Test
	public void field() {

		final ScopeRefNode ref = parse(":: ");

		assertEquals(ScopeType.PARENT, ref.getType());
		assertRange(0, 2, ref);
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void implied() {

		final ScopeRefNode ref = parse("* ");

		assertEquals(ScopeType.IMPLIED, ref.getType());
		assertRange(0, 1, ref);
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void module() {

		final ScopeRefNode ref = parse("$ ");

		assertEquals(ScopeType.MODULE, ref.getType());
		assertRange(0, 1, ref);
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void root() {

		final ScopeRefNode ref = parse("$$ ");

		assertEquals(ScopeType.ROOT, ref.getType());
		assertRange(0, 2, ref);
		assertEquals(3, this.worker.position().offset());
	}

	private ScopeRefNode parse(String text) {
		return parse(Grammar.scopeRef(), text);
	}

}
