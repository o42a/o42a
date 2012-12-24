/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NumberNode;


public final class IntegerNumberNodeMatcher<T extends Node>
		extends BaseMatcher<T> {

	@SuppressWarnings("rawtypes")
	public static final IntegerNumberNodeMatcher<?> INTEGER_NUMBER =
			new IntegerNumberNodeMatcher();

	private IntegerNumberNodeMatcher() {
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof NumberNode)) {
			return false;
		}

		final NumberNode number = (NumberNode) item;

		return number.getFractional() == null && number.getExponent() == null;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Integer number");
	}

}
