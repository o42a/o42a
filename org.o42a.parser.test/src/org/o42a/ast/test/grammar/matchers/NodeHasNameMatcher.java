/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import static org.o42a.ast.test.grammar.GrammarTestCase.canonicalName;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.ref.MemberRefNode;


public final class NodeHasNameMatcher<T extends Node> extends BaseMatcher<T> {

	private final String name;

	public NodeHasNameMatcher(String name) {
		this.name = name;
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof MemberRefNode)) {
			return false;
		}

		final MemberRefNode node = (MemberRefNode) item;

		return canonicalName(node.getName()).equals(this.name);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(this.name);
	}

}
