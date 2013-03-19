/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

		assertThat(
				to(ScopeRefNode.class, ref.getOwner()).getType(),
				is(ScopeType.LOCAL));
		assertThat(canonicalName(ref.getName()), is("foo"));
	}

	@Test
	public void rootFieldRef() {

		final MemberRefNode ref = parse("$$foo");

		assertThat(
				to(ScopeRefNode.class, ref.getOwner()).getType(),
				is(ScopeType.ROOT));
		assertThat(canonicalName(ref.getName()), is("foo"));
	}

	@Test
	public void fieldFieldRef() {

		final MemberRefNode ref = parse("::foo");

		assertThat(
				to(ScopeRefNode.class, ref.getOwner()).getType(),
				is(ScopeType.PARENT));
		assertThat(canonicalName(ref.getName()), is("foo"));
	}

	@Test
	public void impliedFieldRef() {

		final MemberRefNode ref = parse("*foo");

		assertThat(
				to(ScopeRefNode.class, ref.getOwner()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(canonicalName(ref.getName()), is("foo"));
	}

	@Test
	public void selfFieldRef() {

		final MemberRefNode ref = parse(":foo");

		assertThat(
				to(ScopeRefNode.class, ref.getOwner()).getType(),
				is(ScopeType.SELF));
		assertThat(canonicalName(ref.getName()), is("foo"));
	}

	private MemberRefNode parse(String text) {
		return to(MemberRefNode.class, parse(Grammar.ref(), text));
	}

}
