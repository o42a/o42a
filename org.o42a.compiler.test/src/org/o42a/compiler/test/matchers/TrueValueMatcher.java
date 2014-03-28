/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.core.value.Value;


public class TrueValueMatcher extends BaseMatcher<Value<?>> {

	public static final TrueValueMatcher TRUE_VALUE_MATCHER =
			new TrueValueMatcher();

	private TrueValueMatcher() {
	}

	@Override
	public boolean matches(Object item) {

		final Value<?> value = (Value<?>) item;

		return value.getKnowledge().getCondition().isTrue();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("TRUE value");
	}

}
