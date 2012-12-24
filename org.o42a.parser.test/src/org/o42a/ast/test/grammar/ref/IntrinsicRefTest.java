/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class IntrinsicRefTest extends GrammarTestCase {

	@Test
	public void plain() {

		final IntrinsicRefNode ref = parse("$ foo $ /* */");

		assertThat(ref, notNullValue());
		assertThat(ref, hasRange(0, 7));
		assertThat(canonicalName(ref.getName()), is("foo"));
		assertThat(ref.getPrefix(), hasRange(0, 1));
		assertThat(ref.getName(), hasRange(2, 5));
		assertThat(ref.getSuffix(), hasRange(6, 7));
	}

	@Test
	public void intrinsicField() {

		final MemberRefNode result =
				to(MemberRefNode.class, parse(ref(), "$foo$ bar"));

		assertThat(canonicalName(result.getName()), is("bar"));

		final IntrinsicRefNode owner =
				to(IntrinsicRefNode.class, result.getOwner());

		assertThat(canonicalName(owner.getName()), is("foo"));
	}

	private IntrinsicRefNode parse(String text) {
		return parse(Grammar.intrinsicRef(), text);
	}

}
