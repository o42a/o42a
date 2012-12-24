/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;


public final class NodeRangeMatcher<T extends Node> extends BaseMatcher<T> {

	private final long from;
	private final long to;

	public NodeRangeMatcher(long from, long to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("[").appendValue(this.from);
		description.appendText(",").appendValue(this.to);
		description.appendText("]");
	}

	@Override
	public boolean matches(Object item) {

		final Node node = (Node) item;

		if (node.getStart().getOffset() != this.from) {
			return false;
		}
		return node.getEnd().getOffset() == this.to;
	}

}
