/*
    Compiler Tests
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.test.imperative;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class ImperativeStatementErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterReturn() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({= 1, void})");
	}

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({..., void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedRepeat() {
		expectError("compiler.unreachable_command");
		compile("A := integer (Foo: {(... foo), void})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({(!), void})");
	}

	@Test
	public void unreachableAfterUnconditionalNamedExit() {
		expectError("compiler.unreachable_command");
		compile("A := integer (Foo: {(... foo!), void})");
	}

	@Test
	public void unreachableAfterReturnAlts() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({(= 1; = 2), void})");
	}

	@Test
	public void unreachableAfterReturnAndRepeatAlts() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({(False, = 1; void, ...), void})");
	}

	@Test
	public void unreachableAfterExitAndRepeatAlts() {
		expectError("compiler.unreachable_command");
		compile("A := integer ({(False, (!); void, ...), void})");
	}

}
