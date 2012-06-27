/*
    Parser Tests
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

	private final LinkedList<String> expectedErrors = new LinkedList<String>();

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
