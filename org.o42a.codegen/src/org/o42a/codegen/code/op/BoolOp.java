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

import org.o42a.codegen.code.*;
import org.o42a.util.string.ID;


public abstract class BoolOp implements Op {

	public static final ID TRUE_ID = ID.rawId("true");
	public static final ID FALSE_ID = ID.rawId("false");
	public static final ID NOT_ID = ID.rawId("not");

	private static final ID TO_ID = ID.id().detail("to");

	public abstract <O extends Op> O select(
			ID id,
			Code code,
			O trueValue,
			O falseValue);

	public final CondBlock branch(Block source) {
		return branch(source, null);
	}

	public final CondBlock branch(Block source, String conditionName) {
		return branch(source, conditionName, null);
	}

	public final CondBlock branch(
			Block source,
			String trueName,
			String falseName) {
		return branch(
				source,
				trueName != null ? ID.id(trueName) : null,
				falseName != null ? ID.id(falseName) : null);
	}

	public final CondBlock branch(Block source, ID trueName, ID falseName) {

		final OpBlockBase src = source;

		return src.choose(
				this,
				trueName != null ? trueName : TRUE_ID,
				falseName != null ? falseName
				: (trueName != null ? NOT_ID.sub(trueName) : FALSE_ID));
	}

	public final void go(Block source, CodePos truePos) {
		go(source, truePos, null);
	}

	public final void goUnless(Block source, CodePos falsePos) {
		go(source, null, falsePos);
	}

	public final void go(Block source, CodePos truePos, CodePos falsePos) {

		final OpBlockBase s = source;

		if (source.getGenerator().isProxied()) {
			source.writer().go(
					this,
					s.unwrapPos(truePos),
					s.unwrapPos(falsePos));
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
		source.writer().go(
				this,
				exitPos(source, innermostAllocator, truePos, !included),
				exitPos(source, innermostAllocator, falsePos, !included));
	}

	public abstract void returnValue(Block code);

	private static final Allocator allocatorOf(Block source, CodePos pos) {
		if (pos == null) {
			return source.getAllocator();
		}
		return pos.code().getAllocator();
	}

	private static boolean contains(
			Allocator allocator1,
			Allocator allocator2) {

		Allocator allocator = allocator2;

		do {
			if (allocator == allocator1) {
				return true;
			}
			allocator = allocator.getEnclosingAllocator();
		} while (allocator != null);

		return false;
	}

	private static CodePos exitPos(
			Block source,
			Allocator fromAllocator,
			CodePos pos,
			boolean includingFrom) {
		if (pos == null) {
			return null;
		}

		final OpBlockBase s = source;
		final Allocator from;

		if (includingFrom) {
			from = fromAllocator;
		} else {
			// The source allocations already disposed.
			// Start from the enclosing allocator.
			from = fromAllocator.getEnclosingAllocator();
			if (from == null) {
				return s.unwrapPos(pos);
			}
		}

		final Block exitBlock =
				source.addBlock(TO_ID.detail(pos.code().getId()));

		exitBlock.disposeFromTo(from, pos);

		if (!exitBlock.exists()) {
			return s.unwrapPos(pos);
		}

		exitBlock.writer().go(s.unwrapPos(pos));

		return s.unwrapPos(exitBlock.head());
	}

}
