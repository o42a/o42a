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


public class ImperativeSentenceErrorTest extends CompilerTestCase {

	@Test
	public void unreachableAfterUnconditionalReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False, = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundReturn() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void",
				"  = 2",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterUnconditionalRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  ...",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False ...",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundRepeat() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void",
				"  ...",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterUnconditionalExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  !",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterConditionalExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False!",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterCompoundExit() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False? Void. False",
				"  False!",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterReturnOpposites() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  = 2 | = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterRepeatAndReturnOpposites() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False ... | = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterLoopAndRepeatOpposites() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False ... | !",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterReturnAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  = 2 ; = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterRepeatAndReturnAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False ... ; = 3",
				"  Void",
				"})");
	}

	@Test
	public void unreachableAfterLoopAndRepeatAlts() {
		expectError("compiler.unreachable_sentence");
		compile(
				"A := integer ({",
				"  False ... ; !",
				"  Void",
				"})");
	}

}
