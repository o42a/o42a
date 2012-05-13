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

import org.o42a.codegen.code.Allocator;


final class CAllocatorCode extends CAllocator<Allocator> {

	private final CBlock<?> enclosing;

	CAllocatorCode(CBlock<?> enclosing, Allocator allocator) {
		super(enclosing.getBackend(), enclosing.getFunction(), allocator);
		this.enclosing = enclosing;
		new JumpBE.Fake(enclosing.nextPart(), head());
		enclosing.resetNextPart();
	}

	public final CBlock<?> getEnclosing() {
		return this.enclosing;
	}

	@Override
	protected CBlockPart createFirstBlock() {
		return new CAllocatorPart(this);
	}

}
