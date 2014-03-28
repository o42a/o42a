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
		compile("Sub := \"asubc\" [1...4)");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("sub"));
	}

	@Test
	public void leadingSubstring() {
		compile("Sub := \"asubc\" (...4)");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asub"));
	}

	@Test
	public void trailingSubstring() {
		compile("Sub := \"asubc\" [1...)");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("subc"));
	}

	@Test
	public void fullSubstring() {
		compile("Sub := \"asubc\" (...)");

		assertThat(definiteValue(field("sub"), ValueType.STRING), is("asubc"));
	}

	@Test
	public void invalidRange() {
		expectError("compiler.invalid_substr_range");

		compile("Sub := \"asubc\" [4...3)");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void negativeFrom() {
		expectError("compiler.invalid_substr_from");

		compile("Sub := \"asubc\" [-1...4)");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void invalidTo() {
		expectError("compiler.invalid_substr_to");

		compile("Sub := \"asubc\" [1...6)");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void falseString() {
		compile(
				"Use namespace 'Test'",
				"Sub := string (False) [rt-integer '1'...rt-integer '4')");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void runtimeString() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc' [1...4)");

		assertThat(valueOf(field("sub"), ValueType.STRING), runtimeValue());
	}

	@Test
	public void falseFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc' [integer(False)...rt-integer '4')");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void runtimeFrom() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\" [rt-integer '1'...4)");

		assertThat(valueOf(field("sub"), ValueType.STRING), runtimeValue());
	}

	@Test
	public void falseTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := rt-string 'asubc' [rt-integer '1'...integer(False))");

		assertThat(valueOf(field("sub"), ValueType.STRING), falseValue());
	}

	@Test
	public void runtimeTo() {
		compile(
				"Use namespace 'Test'",
				"Sub := \"asubc\" [1...rt-integer '4')");

		assertThat(valueOf(field("sub"), ValueType.STRING), runtimeValue());
	}

}
