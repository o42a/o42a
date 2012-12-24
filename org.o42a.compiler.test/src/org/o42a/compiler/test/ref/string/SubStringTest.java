/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class SubStringTest extends CompilerTestCase {

	@Test
	public void substring() {
		compile("Sub := \"asubc\": substring _from [1] to [4]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("sub"));
	}

	@Test
	public void leadingSubstring() {
		compile("Sub := \"asubc\": substring _to [4]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asub"));
	}

	@Test
	public void trailingSubstring() {
		compile("Sub := \"asubc\": substring _from [1]");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("subc"));
	}

	@Test
	public void fullSubstring() {
		compile("Sub := \"asubc\": substring()");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asubc"));
	}

	@Test
	public void invalidRange() {
		expectError("compiler.invalid_substr_range");

		compile("Sub := \"asubc\": substring _ from [4] to [3]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void negativeFrom() {
		expectError("compiler.invalid_substr_from");

		compile("Sub := \"asubc\": substring _ from [-1] to [4]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void invalidTo() {
		expectError("compiler.invalid_substr_to");

		compile("Sub := \"asubc\": substring _ from [1] to [6]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseString() {
		compile(
				"Use namespace 'Test'",
				"Sub := string(False): substring",
				"_ from [rt-integer '1']",
				"_ to [rt-integer '4']");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [1]",
				"_ to [4]");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [integer(False)]",
				"_ to [rt-integer '4']");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\": substring",
				"_ from [rt-integer '1']",
				"_ to [4]");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void falseTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc': substring",
				"_ from [rt-integer '1']",
				"_ to [integer(False)]");

		assertFalseValue(valueOf(field("sub"), ValueType.STRING));
	}

	@Test
	public void runtimeTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\": substring",
				"_ from [1]",
				"_ to [rt-integer '4']");

		assertRuntimeValue(valueOf(field("sub"), ValueType.STRING));
	}

}
