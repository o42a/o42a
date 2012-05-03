/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.OpBlockBase.unwrapPos;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;


public abstract class BoolOp implements Op {

	public abstract <O extends Op> O select(
			CodeId id,
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
				trueName != null ? source.getGenerator().id(trueName) : null,
				falseName != null ? source.getGenerator().id(falseName) : null);
	}

	public final CondBlock branch(
			Block source,
			CodeId trueName,
			CodeId falseName) {

		final OpBlockBase src = source;

		return src.choose(
				this,
				trueName != null ? trueName : source.getGenerator().id("true"),
				falseName != null
				? falseName
				: (trueName != null
						? source.getGenerator().id("not_" + trueName)
						: source.getGenerator().id("false")));
	}

	public final void go(Block source, CodePos truePos) {
		go(source, truePos, null);
	}

	public final void goUnless(Block source, CodePos falsePos) {
		go(source, null, falsePos);
	}

	public final void go(Block source, CodePos truePos, CodePos falsePos) {

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

		final Allocator from;

		if (includingFrom) {
			from = fromAllocator;
		} else {
			// The source allocations already disposed.
			// Start from the enclosing allocator.
			from = fromAllocator.getEnclosingAllocator();
			if (from == null) {
				return unwrapPos(pos);
			}
		}

		final Block exitBlock = source.addBlock(
				source.id()
				.detail("to")
				.detail(pos.code().getId()));

		exitBlock.disposeFromTo(from, pos);
		exitBlock.writer().go(unwrapPos(pos));

		return unwrapPos(exitBlock.head());
	}

}
