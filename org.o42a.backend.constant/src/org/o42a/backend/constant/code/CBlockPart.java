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


public abstract class CBlockPart extends CCodePart<Block> {

	private static final byte ENTRY_BLOCK = 0x01;
	private static final byte ENTRY_PREV_PART = 0x02;
	private static final byte TERMINATED = 0x04;
	private static final byte HAS_ENTRIES = ENTRY_BLOCK;

	private final CCodePos head;
	private CBlockPart nextPart;
	private Block underlying;
	private final int index;
	private byte flags;

	CBlockPart(CBlock<?> block) {
		this(block, block.getId(), 0);
	}

	CBlockPart(CBlock<?> block, CodeId id, int index) {
		super(block, id);
		this.index = index;
		this.head = new CCodePos(this);
	}

	public final CBlock<?> block() {
		return (CBlock<?>) code();
	}

	public final int index() {
		return this.index;
	}

	public final CCodePos head() {
		return this.head;
	}

	public final boolean exists() {
		return (this.flags & HAS_ENTRIES) != 0 || hasOps();
	}

	public final boolean isTerminated() {
		return (this.flags & TERMINATED) != 0;
	}

	public final void terminate() {
		this.flags |= TERMINATED;
	}

	@Override
	public final Block underlying() {
		assert this.underlying != null :
			"Block part not revealed yet: " + this;
		return this.underlying;
	}

	public final void initUnderlying(Block underlyingEnclosing) {
		if (!exists()) {
			assert this.nextPart == null :
				"Block part \"" + this
				+ "\" does not exist, but has continuation";
			return;
		}
		assert this.underlying == null :
			"Block part \"" + getId() + "\" already created";
		this.underlying = createUnderlying(underlyingEnclosing);
		if (this.nextPart != null) {
			this.nextPart.initUnderlying(underlyingEnclosing);
		}
	}

	public final void reveal() {
		if (!exists()) {
			return;
		}
		revealRecords();
		if (this.nextPart != null) {
			if ((this.nextPart.flags & ENTRY_PREV_PART) != 0) {
				underlying().go(this.nextPart.head().getUnderlying());
			}
			this.nextPart.reveal();
		}
	}

	protected abstract CBlockPart newNextPart(int index);

	protected abstract Block createUnderlying(Block underlyingEnclosing);

	final CBlockPart createNextPart(int index) {
		assert this.nextPart == null :
			"Next part of " + this + " already created";
		return this.nextPart = newNextPart(index);
	}

	final void comeFrom(CBlock<?> block) {
		comeFrom(block.nextPart());
	}

	final void comeFrom(CBlockPart from) {
		this.flags |= ENTRY_BLOCK;
	}

	final void comeFromPrev(CBlockPart from) {
		this.flags |= ENTRY_PREV_PART;
	}

}
