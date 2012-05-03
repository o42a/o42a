/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import org.o42a.codegen.code.Block;


final class CAllocatorPart extends CBlockPart {

	CAllocatorPart(CAllocatorCode allocator) {
		super(allocator);
	}

	private CAllocatorPart(CAllocatorCode allocator, int index) {
		super(
				allocator,
				index != 0
				? allocator.getId().anonymous(index) : allocator.getId(),
				index);
	}

	@Override
	protected CBlockPart newNextPart(int index) {
		return new CAllocatorPart(allocator(), index);
	}

	@Override
	protected Block createUnderlying() {

		final CAllocatorCode allocator = allocator();

		if (index() == 0) {
			return allocator.getEnclosing()
					.firstPart()
					.underlying()
					.allocator(allocator.getId().getLocal());
		}

		final Block underlying = allocator.firstPart().underlying();

		return underlying.addBlock(underlying.id().anonymous(index()));
	}

	private final CAllocatorCode allocator() {
		return (CAllocatorCode) code();
	}

}
