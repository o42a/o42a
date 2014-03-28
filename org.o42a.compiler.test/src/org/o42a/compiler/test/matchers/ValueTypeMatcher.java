/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.core.value.ValueType;


public class ValueTypeMatcher extends BaseMatcher<ValueType<?>> {

	private final ValueType<?> valueType;

	public ValueTypeMatcher(ValueType<?> valueType) {
		this.valueType = valueType;
	}

	@Override
	public boolean matches(Object item) {

		final ValueType<?> valueType = (ValueType<?>) item;

		return valueType.is(this.valueType);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(this.valueType.toString());
	}

}
