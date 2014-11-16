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

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.util.string.ID;


final class BoolOpInternals {

	private static final ID EXIT_TO_ID = ID.id().detail("__exit_to__");

	static final Allocator allocatorOf(Block source, CodePos pos) {
		if (pos == null) {
			return source.getClosestAllocator();
		}
		return pos.code().getClosestAllocator();
	}

	static boolean contains(Allocator allocator1, Allocator allocator2) {

		Allocator allocator = allocator2;

		do {
			if (allocator == allocator1) {
				return true;
			}
			allocator = allocator.getEnclosingAllocator();
		} while (allocator != null);

		return false;
	}

	static CodePos exitPos(
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

		return dispose(source, pos, from);
	}

	static CodePos dispose(Block source, CodePos pos, Allocator from) {

		final Block exitBlock =
				source.addBlock(EXIT_TO_ID.detail(pos.code().getId()));
		final OpBlockBase exitBlk = exitBlock;

		exitBlk.disposeFromTo(from, pos);
		exitBlk.addAssetsTo(pos);
		exitBlock.writer().go(exitBlk.unwrapPos(pos));
		exitBlk.removeAllAssets();

		return exitBlk.unwrapPos(exitBlock.head());
	}

	static void internalGo(
			Block source,
			BoolOp op,
			CodePos truePos,
			CodePos falsePos) {

		final OpBlockBase s = source;

		if (truePos != null) {
			s.addAssetsTo(truePos);
		}
		if (falsePos != null) {
			s.addAssetsTo(falsePos);
		}
		source.writer().go(
				op,
				s.unwrapPos(truePos),
				s.unwrapPos(falsePos));
		if (truePos != null && falsePos != null) {
			s.removeAllAssets();
		}
	}

}
