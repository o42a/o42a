/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


final class TestLogger extends TestWatcher implements Logger {

	private final LinkedList<String> expectedErrors = new LinkedList<>();

	public void expectError(String code) {
		this.expectedErrors.addLast("parser." + code);
	}

	@Override
	public void log(LogRecord record) {

		final String code = record.getCode();
		final String expected = this.expectedErrors.poll();

		if (expected == null) {
			fail("Error occurred: " + record);
		}

		assertEquals(
				"Unexpected error occurred: " + record,
				expected,
				code);
	}

	@Override
	protected void succeeded(Description description) {
		assertThat(
				"Errors expected, but not logged: " + this.expectedErrors,
				this.expectedErrors.isEmpty(),
				is(true));
	}

	@Override
	protected void starting(Description description) {
		this.expectedErrors.clear();
	}

}
