/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
		assertThat(ref, hasRange(0, 6));
		assertThat(canonicalName(ref.getName()), is("foo"));
		assertThat(ref.getName(), hasRange(0, 3));
		assertThat(ref.getQualifier(), hasRange(4, 6));
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
