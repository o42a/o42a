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


class CBlockPart extends CCodePart<Block> {

	private final CCodePos head;
	private CBlockPart nextPart;
	private Block underlying;
	private final int index;
	private boolean hasEntries;
	private boolean terminated;

	CBlockPart(CBlock<?> block) {
		this(block, 0);
	}

	private CBlockPart(CBlock<?> block, int index) {
		super(
				block,
				index != 0 ? block.getId().anonymous(index) : block.getId());
		this.index = index;
		this.head = new CCodePos(this);
	}

	public final CBlock<?> block() {
		return (CBlock<?>) code();
	}

	public final CCodePos head() {
		return this.head;
	}

	public final boolean exists() {
		return this.hasEntries || hasOps();
	}

	public final boolean isTerminated() {
		return this.terminated;
	}

	@Override
	public Block underlying() {
		assert this.underlying != null :
			"Block part not revealed yet: " + this;
		return this.underlying;
	}

	public final void initUnderlying(Block underlyingEnclosing) {
		this.underlying = createUnderlying(underlyingEnclosing);
		if (this.nextPart != null) {
			this.nextPart.initUnderlying(underlyingEnclosing);
		}
	}

	public final void reveal() {
		revealRecords();
		if (this.nextPart != null) {
			this.nextPart.reveal();
		}
	}

	protected Block createUnderlying(Block underlyingEnclosing) {

		final CodeId localId = block().getId().getLocal();
		final CodeId partName;

		if (this.index == 0) {
			partName = localId;
		} else {
			partName = localId.anonymous(this.index);
		}

		return underlyingEnclosing.addBlock(partName);
	}

	final CBlockPart createNextPart(int index) {
		assert this.nextPart == null :
			"Next part of " + this + " already created";
		return this.nextPart = new CBlockPart(block(), index);
	}

	final void comeFrom(CBlock<?> block) {
		comeFrom(block.nextPart());
	}

	final void comeFrom(CCodePart<Block> from) {
		this.hasEntries = true;
	}

	final void terminate() {
		this.terminated = true;
	}

}
