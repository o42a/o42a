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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.util.string.ID;


public abstract class OpBlockBase extends Code {

	public OpBlockBase(Code enclosing, ID name) {
		super(enclosing, name);
	}

	public OpBlockBase(Generator generator, ID id) {
		super(generator, id);
	}

	protected abstract CondBlock choose(
			BoolOp condition,
			ID trueName,
			ID falseName);

	protected boolean disposeUpTo(CodePos pos) {
		return disposeFromTo(getClosestAllocator(), pos);
	}

	protected boolean disposeFromTo(Allocator fromAllocator, CodePos pos) {

		final Allocator toAllocator = pos.code().getClosestAllocator();

		if (toAllocator.ptr().is(pos)) {
			// Go to the allocator's head?
			disposeFromIncludingTo(fromAllocator, toAllocator);
			return true;
		}

		disposeFromExcludingTo(fromAllocator, toAllocator);
		return false;
	}

	protected abstract CodePos unwrapPos(CodePos pos);

	protected abstract void addAssetsTo(CodePos pos);

	@Override
	protected void removeAllAssets() {
		super.removeAllAssets();
	}

	protected abstract void disposeBy(Allocator allocator);

	private void disposeFromIncludingTo(
			final Allocator fromAllocator,
			final Allocator toAllocator) {

		Allocator allocator = fromAllocator;

		for (;;) {
			disposeBy(allocator);
			if (allocator == toAllocator) {
				break;
			}
			allocator = allocator.getEnclosingAllocator();
			assert allocator != null :
				fromAllocator + " is not inside " + toAllocator;
		}
	}

	private void disposeFromExcludingTo(
			final Allocator fromAllocator,
			final Allocator toAllocator) {

		Allocator allocator = fromAllocator;

		while (allocator != toAllocator) {
			disposeBy(allocator);
			allocator = allocator.getEnclosingAllocator();
			assert allocator != null :
				fromAllocator + " is not inside " + toAllocator;
		}
	}

}
