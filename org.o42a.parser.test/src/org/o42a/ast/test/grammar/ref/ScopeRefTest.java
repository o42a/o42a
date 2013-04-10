/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class ScopeRefTest extends GrammarTestCase {

	@Test
	public void implied() {

		final ScopeRefNode ref = parse("* ");

		assertEquals(ScopeType.IMPLIED, ref.getType());
		assertThat(ref, hasRange(0, 1));
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void self() {

		final ScopeRefNode ref = parse(": ");

		assertEquals(ScopeType.SELF, ref.getType());
		assertThat(ref, hasRange(0, 1));
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void selfEOF() {

		final ScopeRefNode ref = parse(":");

		assertEquals(ScopeType.SELF, ref.getType());
		assertThat(ref, hasRange(0, 1));
	}

	@Test
	public void parent() {

		final ScopeRefNode ref = parse(":: ");

		assertEquals(ScopeType.PARENT, ref.getType());
		assertThat(ref, hasRange(0, 2));
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void macros() {

		final ScopeRefNode ref = parse("## ");

		assertEquals(ScopeType.MACROS, ref.getType());
		assertThat(ref, hasRange(0, 2));
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void local() {

		final ScopeRefNode ref = parse("$ ");

		assertEquals(ScopeType.LOCAL, ref.getType());
		assertThat(ref, hasRange(0, 1));
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void anonymous() {

		final ScopeRefNode ref = parse("$: ");

		assertEquals(ScopeType.ANONYMOUS, ref.getType());
		assertThat(ref, hasRange(0, 2));
		assertEquals(3, this.worker.position().offset());
	}

	@Test
	public void module() {

		final ScopeRefNode ref = parse("/ ");

		assertEquals(ScopeType.MODULE, ref.getType());
		assertThat(ref, hasRange(0, 1));
		assertEquals(2, this.worker.position().offset());
	}

	@Test
	public void root() {

		final ScopeRefNode ref = parse("// ");

		assertEquals(ScopeType.ROOT, ref.getType());
		assertThat(ref, hasRange(0, 2));
		assertEquals(3, this.worker.position().offset());
	}

	private ScopeRefNode parse(String text) {
		return parse(Grammar.scopeRef(), text);
	}

}
