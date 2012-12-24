/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.operator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class IntegerCompareTest extends CompilerTestCase {

	@Test
	public void less() {
		assertTrueVoid(compare("Result := 1 < 2"));
		assertFalseVoid(compare("Result := 2 < 1"));
	}

	@Test
	public void lessOrEqual() {
		assertTrueVoid(compare("Result := 1 <= 2"));
		assertFalseVoid(compare("Result := 2 <= 1"));
	}

	@Test
	public void greater() {
		assertTrueVoid(compare("Result := 2 > 1"));
		assertFalseVoid(compare("Result := 1 > 2"));
	}

	@Test
	public void greaterOrEqual() {
		assertTrueVoid(compare("Result := 2 >= 1"));
		assertFalseVoid(compare("Result := 1 >= 2"));
	}

	@Test
	public void equal() {
		assertTrueVoid(compare("Result := 2 == 2"));
		assertFalseVoid(compare("Result := 1 == 2"));
	}

	@Test
	public void notEqual() {
		assertTrueVoid(compare("Result := 1 <> 2"));
		assertFalseVoid(compare("Result := 1 <> 1"));
	}

	@Test
	public void compare() {
		assertThat(
				definiteValue(
						compare("Result := 1 <=> 12"),
						ValueType.INTEGER),
				is(-1L));
		assertThat(
				definiteValue(
						compare("Result := 100 <=> 12"),
						ValueType.INTEGER),
				is(1L));
		assertThat(
				definiteValue(
						compare("Result := -24 <=> -24"),
						ValueType.INTEGER),
				is(0L));
	}

	private Obj compare(String line, String... lines) {
		compile(line, lines);
		return field("result").toObject();
	}

}
