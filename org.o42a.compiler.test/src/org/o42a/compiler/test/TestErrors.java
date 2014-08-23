/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.o42a.intrinsic.CompileErrors;
import org.o42a.util.log.LogMessage;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


final class TestErrors extends TestWatcher implements Logger, CompileErrors {

	private final LinkedList<String> expectedErrors = new LinkedList<>();
	private boolean hasErrors;

	@Override
	public boolean hasCompileErrors() {
		return this.hasErrors;
	}

	@Override
	public void log(LogRecord record) {

		final LogMessage message = record.getMessage();

		if (message.getSeverity().isError()) {
			this.hasErrors = true;
		}

		final String expected = this.expectedErrors.poll();

		if (expected == null) {
			fail("Error occurred: " + record);
		}

		assertThat(
				"Unexpected error occurred: " + record,
				message.getCode(),
				is(expected));
	}

	public final void expectError(String code) {
		this.expectedErrors.add(code);
	}

	@Override
	public String toString() {
		return this.expectedErrors.toString();
	}

	@Override
	protected void succeeded(Description description) {
		assertThat(
				"Errors expected, but not logged: " + this,
				this.expectedErrors.isEmpty(),
				is(true));
	}

	@Override
	protected void starting(Description description) {
		this.hasErrors = false;
		this.expectedErrors.clear();
	}

}
