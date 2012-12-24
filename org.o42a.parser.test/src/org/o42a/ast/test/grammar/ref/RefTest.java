/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class RefTest extends GrammarTestCase {

	@Test
	public void parentScopedRef() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("::foo"));
		final ScopeRefNode owner = to(ScopeRefNode.class, ref.getOwner());

		assertThat(canonicalName(ref.getName()), is("foo"));
		assertThat(ref.getQualifier(), nullValue());
		assertThat(ref.getDeclaredIn(), nullValue());
		assertThat(this.worker.position().offset(), is(5L));
		assertThat(ref, hasRange(0, 5));
		assertThat(owner.getType(), is(ScopeType.PARENT));
		assertThat(owner, hasRange(0, 2));
	}

	@Test
	public void parentRef() {

		final MemberRefNode ref = to(MemberRefNode.class, parse("foo::bar"));
		final ParentRefNode owner = to(ParentRefNode.class, ref.getOwner());

		assertThat(canonicalName(ref.getName()), is("bar"));
		assertThat(ref.getQualifier(), nullValue());
		assertThat(ref.getDeclaredIn(), nullValue());
		assertEquals(8, this.worker.position().offset());
		assertThat(ref, hasRange(0, 8));
		assertThat(canonicalName(owner.getName()), is("foo"));
		assertThat(owner, hasRange(0, 5));
		assertThat(owner.getQualifier(), hasRange(3, 5));
	}

	private RefNode parse(String text) {
		return parse(Grammar.ref(), text);
	}

}
