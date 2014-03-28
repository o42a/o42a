/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class ValueOfTypeMatcher<T> extends BaseMatcher<Value<T>> {

	private final ValueType<T> valueType;

	public ValueOfTypeMatcher(ValueType<T> valueType) {
		this.valueType = valueType;
	}

	@Override
	public boolean matches(Object item) {

		@SuppressWarnings("unchecked")
		final Value<T> value = (Value<T>) item;

		return value.getValueType().is(this.valueType);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Value of type ").appendValue(this.valueType);
	}

}
