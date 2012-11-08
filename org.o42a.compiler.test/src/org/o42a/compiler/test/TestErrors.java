/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.compiler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.o42a.intrinsic.CompileErrors;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


final class TestErrors extends TestWatcher implements Logger, CompileErrors {

	private final LinkedList<String> expectedErrors =
			new LinkedList<String>();
	private boolean hasErrors;

	@Override
	public boolean hasCompileErrors() {
		return this.hasErrors;
	}

	@Override
	public void log(LogRecord record) {
		if (record.getSeverity().isError()) {
			this.hasErrors = true;
		}

		final String code = record.getCode();
		final String expected = this.expectedErrors.poll();

		if (expected == null) {
			fail("Error occurred: " + record);
		}

		assertThat(
				"Unexpected error occurred: " + record,
				expected,
				is(code));
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
