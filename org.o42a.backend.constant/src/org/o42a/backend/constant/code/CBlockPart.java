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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;


class CBlockPart extends CCodePart<Block> {

	private final CCodePos head;
	private CBlockPart nextPart;

	CBlockPart(CBlock<?> block) {
		this(block, block.getId());
	}

	private CBlockPart(CCode<?> code, CodeId id) {
		super(code, id);
		this.head = new CCodePos(this);
	}

	public final CBlock<?> block() {
		return (CBlock<?>) code();
	}

	public final CCodePos head() {
		return this.head;
	}

	@Override
	protected Block createUnderlying(Code underlying) {
		return underlying.addBlock(getId().getLocal());
	}

	final CBlockPart createNextPart(CodeId id) {
		assert this.nextPart != null :
			"Next part of " + this + " already created";
		return this.nextPart = new CBlockPart(block(), id);
	}

	final CBlockPart getNextPart() {
		return this.nextPart;
	}

}
