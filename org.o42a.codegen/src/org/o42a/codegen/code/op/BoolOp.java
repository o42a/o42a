/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.codegen.code.op;

import static org.o42a.codegen.code.op.BoolOpInternals.*;

import org.o42a.codegen.code.*;
import org.o42a.util.string.ID;


public interface BoolOp extends Op {

	ID TRUE_ID = ID.rawId("__true__");
	ID FALSE_ID = ID.rawId("__false__");
	ID NOT_ID = ID.rawId("__not__");

	<O extends Op> O select(
			ID id,
			Code code,
			O trueValue,
			O falseValue);

	default CondBlock branch(Block source) {
		return branch(source, null);
	}

	default CondBlock branch(Block source, String conditionName) {
		return branch(source, conditionName, null);
	}

	default CondBlock branch(
			Block source,
			String trueName,
			String falseName) {
		return branch(
				source,
				trueName != null ? ID.id(trueName) : null,
				falseName != null ? ID.id(falseName) : null);
	}

	default CondBlock branch(Block source, ID trueName, ID falseName) {

		final OpBlockBase src = source;

		return src.choose(
				this,
				trueName != null ? trueName : TRUE_ID,
				falseName != null ? falseName
				: (trueName != null ? NOT_ID.sub(trueName) : FALSE_ID));
	}

	default void go(Block source, CodePos truePos) {
		go(source, truePos, null);
	}

	default void goUnless(Block source, CodePos falsePos) {
		go(source, null, falsePos);
	}

	default void go(Block source, CodePos truePos, CodePos falsePos) {
		if (source.getGenerator().isProxied()) {
			internalGo(source, this, truePos, falsePos);
			return;
		}

		final Allocator allocator1 = allocatorOf(source, truePos);
		final Allocator allocator2 = allocatorOf(source, falsePos);
		final Allocator innermostAllocator;
		final CodePos innermostPos;

		// Find the innermost allocator.
		if (contains(allocator2, allocator1)) {
			innermostAllocator = allocator1;
			innermostPos = truePos;
		} else {
			assert contains(allocator1, allocator2) :
				"Neither " + truePos + " allocator includes the " + falsePos
				+ " one, nor vice versa";
			innermostAllocator = allocator2;
			innermostPos = falsePos;
		}

		// Dispose up to the innermost allocator.
		final OpBlockBase src = source;
		final boolean included;

		if (innermostPos == null) {
			included = false;
		} else {
			included = src.disposeUpTo(innermostPos);
		}

		// Create an auxiliary block either for true or false position
		// if necessary. This block will dispose the rest of the allocations
		// made between innermost position and the target one.
		internalGo(
				source,
				this,
				exitPos(source, innermostAllocator, truePos, !included),
				exitPos(source, innermostAllocator, falsePos, !included));
	}

	default void returnValue(Block code) {
		returnValue(code, true);
	}

	void returnValue(Block code, boolean dispose);

}
