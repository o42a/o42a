/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignOfNumber;


public class NumberNodeSignMatcher<T extends Node>
		extends BaseMatcher<T> {

	@SuppressWarnings("rawtypes")
	public static final NumberNodeSignMatcher<?> UNSIGNED_NUMBER =
			new NumberNodeSignMatcher(null);
	@SuppressWarnings("rawtypes")
	public static final NumberNodeSignMatcher<?> NEGATIVE_NUMBER =
			new NumberNodeSignMatcher(SignOfNumber.NEGATIVE_NUMBER);
	@SuppressWarnings("rawtypes")
	public static final NumberNodeSignMatcher<?> POSITIVE_NUMBER =
			new NumberNodeSignMatcher(SignOfNumber.POSITIVE_NUMBER);

	private final SignOfNumber sign;

	private NumberNodeSignMatcher(SignOfNumber sign) {
		this.sign = sign;
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof NumberNode)) {
			return false;
		}

		final NumberNode number = (NumberNode) item;
		final SignNode<SignOfNumber> sign = number.getSign();

		if (sign == null) {
			return this.sign == null;
		}

		return this.sign == sign.getType();
	}

	@Override
	public void describeTo(Description description) {
		if (this.sign == null) {
			description.appendText("Unsigned number");
		} else if (this.sign.isNegative()) {
			description.appendText("Negative number");
		} else {
			description.appendText("Positive number");
		}
	}

}
