/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.atom.DigitsNode;
import org.o42a.ast.atom.NumberNode;


public final class NumberNodeHasIntegerMatcher<T extends Node>
		extends BaseMatcher<T> {

	private final String value;

	public NumberNodeHasIntegerMatcher(String value) {
		this.value = value;
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof NumberNode)) {
			return false;
		}

		final NumberNode number = (NumberNode) item;
		final DigitsNode integer = number.getInteger();

		return integer != null && integer.getDigits().equals(this.value);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(this.value);
	}

}
