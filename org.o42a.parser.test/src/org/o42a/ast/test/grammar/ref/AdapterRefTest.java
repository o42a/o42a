/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.AdapterRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class AdapterRefTest extends GrammarTestCase {

	@Test
	public void adapterRef() {

		final AdapterRefNode ref = parse("foo@@bar");

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(ref.getAdapterId().getPrefix(), hasRange(3, 5));
		assertThat(ref.getType(), isName("bar"));
		assertThat(ref.getMembership(), nullValue());
	}

	@Test
	public void qualifiedAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@baz");

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(ref.getAdapterId().getPrefix(), hasRange(3, 5));

		final MemberRefNode type = to(MemberRefNode.class, ref.getType());

		assertThat(type, isName("bar"));

		assertThat(ref.getMembership().getPrefix(), hasRange(8, 9));
		assertThat(ref.getDeclaredIn(), isName("baz"));
	}

	@Test
	public void doubleAdapterRef() {

		final AdapterRefNode ref = parse("foo@@bar@@baz@type");
		final AdapterRefNode owner = to(AdapterRefNode.class, ref.getOwner());

		assertThat(owner.getOwner(), isName("foo"));
		assertThat(owner.getAdapterId().getPrefix(), hasRange(3, 5));

		assertThat(owner.getType(), isName("bar"));

		assertThat(ref.getAdapterId().getPrefix(), hasRange(8, 10));
		assertThat(
				canonicalName(
						to(MemberRefNode.class, ref.getType()).getName()),
				is("baz"));
		assertThat(ref.getMembership().getPrefix(), hasRange(13, 14));
		assertThat(ref.getDeclaredIn(), isName("type"));
	}

	@Test
	public void parentheses() {

		final AdapterRefNode ref = parse("foo@@(bar)@(type)");
		final MemberRefNode owner = to(MemberRefNode.class, ref.getOwner());

		assertThat(canonicalName(owner.getName()), is("foo"));
		assertThat(ref.getAdapterId().getType(), isName("bar"));
		assertThat(ref.getDeclaredIn(), isName("type"));
	}

	@Test
	public void qualifiedType() {

		final AdapterRefNode ref = parse("foo@@(bar@type)");
		final MemberRefNode type =
				to(MemberRefNode.class, ref.getAdapterId().getType());

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(canonicalName(type.getName()), is("bar"));
		assertThat(type.getDeclaredIn(), isName("type"));
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlBeforeQualifier() {

		final RefNode ref = parse(ref(), "foo\n@@");

		assertThat(ref, isName("foo"));
	}

	@Test
	public void nlAfterQualifier() {
		expectError("missing_type");

		final AdapterRefNode ref = parse("foo@@\nbar");

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(ref.getType(), nullValue());
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlBeforeRetention() {

		final AdapterRefNode ref = parse("foo @@bar \n@baz");

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(ref.getType(), isName("bar"));
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	@Test
	public void nlAfterRetention() {
		expectError("missing_type");

		final AdapterRefNode ref = parse("foo @@bar @\nbaz");

		assertThat(ref.getOwner(), isName("foo"));
		assertThat(ref.getType(), isName("bar"));
		assertThat(ref.getDeclaredIn(), nullValue());
	}

	private AdapterRefNode parse(String text) {
		return to(AdapterRefNode.class, parse(ref(), text));
	}

}
