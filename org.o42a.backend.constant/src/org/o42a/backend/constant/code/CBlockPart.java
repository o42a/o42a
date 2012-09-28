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
import org.o42a.util.string.ID;


public abstract class CBlockPart extends CCodePart<Block> {

	private static final short ENTRY_BLOCK = 0x01;
	private static final short ENTRY_PREV_PART = 0x02;
	private static final short MULTIPLE_ENTRIES = 0x04;
	private static final short JOINED = 0x08;
	private static final short REVEALED = 0x10;

	private static final short HAS_ENTRIES = ENTRY_BLOCK;

	private final CCodePos head;
	private EntryBE firstEntry;
	private CBlockPart nextPart;
	private TermBE terminator;
	private Block underlying;
	private final int index;
	private short flags;

	CBlockPart(CBlock<?> block) {
		this(block, block.getId(), 0);
	}

	CBlockPart(CBlock<?> block, ID id, int index) {
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

	public final boolean hasOps() {
		return isTerminated() || hasRecords();
	}

	public final boolean exists() {
		return (this.flags & HAS_ENTRIES) != 0 || hasOps();
	}

	public final boolean isTerminated() {
		return this.terminator != null;
	}

	@Override
	public final Block underlying() {
		if (this.underlying != null) {
			return this.underlying;
		}
		return this.underlying = initUnderlying();
	}

	@Override
	public boolean revealUpTo(OpRecord last) {
		revealJoinedTo();
		return super.revealUpTo(last);
	}

	@Override
	protected void revealRecords() {
		revealJoinedTo();
		super.revealRecords();
	}

	protected abstract CBlockPart newNextPart(int index);

	protected abstract Block createUnderlying();

	final CBlockPart createNextPart(int index) {
		assert this.nextPart == null :
			"Next part of " + this + " already created";
		return this.nextPart = newNextPart(index);
	}

	final void comeFrom(EntryBE entry) {
		if (this.firstEntry == null) {
			this.firstEntry = entry;
		} else {
			this.flags |= MULTIPLE_ENTRIES;
		}
		if (entry.continuation()) {
			this.flags |= ENTRY_PREV_PART;
		} else {
			this.flags |= ENTRY_BLOCK;
		}
	}

	final CBlockPart terminate(TermBE terminator) {
		assert !isTerminated() :
			this + " is terminated already";
		this.terminator = terminator;
		return this;
	}

	final void prepare() {
		prepareRecords();
		if (this.nextPart != null) {
			this.nextPart.prepare();
		}
		if (this.terminator != null) {
			this.terminator.prepare();
		}
	}

	final void reveal() {
		// Do not reveal a joined part explicitly.
		if (!isJoined()) {
			revealPart();
		}
		if (this.nextPart != null) {
			this.nextPart.reveal();
		}
	}

	private final Block initUnderlying() {
		if (!exists()) {
			assert this.nextPart == null :
				"Block part \"" + this
				+ "\" does not exist, but has continuation";
			return null;
		}
		assert isTerminated() :
			this + " does not have terminator";
		assert this.underlying == null :
			"Underlying block already created for " + this;

		final CBlockPart joinedTo = join();

		if (joinedTo != null) {
			this.flags |= JOINED;
			return joinedTo.underlying();
		}

		return createUnderlying();
	}

	private final boolean isJoined() {
		underlying();
		return (this.flags & JOINED) != 0;
	}

	private CBlockPart join() {
		if ((this.flags & MULTIPLE_ENTRIES) != 0) {
			return null;// Can only join with single entry part.
		}
		if (this.firstEntry == null) {
			return null;// Only happens for functions.
		}

		final JumpBE jump = this.firstEntry.toJump();

		if (jump.conditional()) {
			return null;// Previous block is branched.
		}

		return jump.part();// Join with entry block.
	}

	private void revealJoinedTo() {
		if (isJoined()) {

			// Reveal the records of the part this one is joined to.
			final CBlockPart joinedTo = join();

			if (!joinedTo.isRevealed()) {
				joinedTo.flags |= REVEALED;
				joinedTo.revealRecords();
			}
		}
	}

	private void revealPart() {
		if (!isRevealed()) {
			revealPartRecords();
		}
		if (this.firstEntry != null || !isEmpty()) {
			revealTerminator();
		}
	}

	private void revealTerminator() {
		assert isTerminated() :
			this + " does not have terminator";

		final CBlockPart joined = nextJoined();

		if (joined != null) {
			joined.revealPart();
		} else {
			this.terminator.reveal();
		}
	}

	private final boolean isRevealed() {
		return (this.flags & REVEALED) != 0;
	}

	private void revealPartRecords() {
		this.flags |= REVEALED;

		if (!exists()) {
			assert this.nextPart == null :
				"Part \"" + this + "\" does not exist, but has a next part";
			return;
		}
		revealRecords();
	}

	private CBlockPart nextJoined() {

		final JumpBE jump = this.terminator.toJump();

		if (jump == null || jump.conditional()) {
			return null;
		}

		final CBlockPart nextPart = jump.target().part();

		if (nextPart == this) {
			return null;
		}
		if (nextPart.underlying() != underlying()) {
			return null;
		}

		assert nextPart.isJoined() :
			nextPart + " expected to be joint with " + this;

		return nextPart;
	}

}
