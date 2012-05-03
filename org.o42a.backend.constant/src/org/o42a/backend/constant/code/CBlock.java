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

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.op.BoolCOp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.util.Chain;


public abstract class CBlock<B extends Block> extends CCode<B>
		implements BlockWriter {

	private CBlockPart firstPart;
	private CBlockPart lastPart;
	private CBlockPart nextPart;

	private final SubBlocks subBlocks = new SubBlocks();
	private CBlock<?> nextBlock;
	private int blockSeq;

	public CBlock(ConstBackend backend, CFunction<?> function, B code) {
		super(backend, function, code);
	}

	@Override
	public final CBlock<B> block() {
		return this;
	}

	@Override
	public boolean created() {
		return this.firstPart != null && this.firstPart.exists();
	}

	@Override
	public boolean exists() {
		if (this.nextPart == null || !this.nextPart.exists()) {
			return false;
		}
		return !this.nextPart.isTerminated();
	}

	@Override
	public final CCodePos head() {
		return firstPart().head();
	}

	@Override
	public CodePos tail() {
		if (this.nextPart != null) {
			if (!this.nextPart.hasOps()) {
				return this.nextPart.head();
			}
			resetNextPart();
		}
		return nextPart().head();
	}

	@Override
	public CBlockPart nextPart() {
		if (this.nextPart != null) {
			return this.nextPart;
		}
		if (this.firstPart == null) {
			return firstPart();
		}

		final CBlockPart prev = this.lastPart;
		final CBlockPart next = addNextPart();

		if (!prev.isTerminated()) {
			new JumpBE.Next(prev, next.head());
		}

		return this.nextPart;
	}

	@Override
	public final CCodeBlock block(Block code) {
		return this.subBlocks.add(new CCodeBlock(this, code));
	}

	@Override
	public AllocatorWriter allocator(Allocator allocator) {
		return this.subBlocks.add(new CAllocatorCode(this, allocator));
	}

	@Override
	public final void go(final CodePos pos) {
		new JumpBE.Unconditional(nextPart(), cast(pos));
		resetNextPart();
	}

	@Override
	public final void go(
			final BoolOp condition,
			final CodePos truePos,
			final CodePos falsePos) {

		final BoolCOp cond = (BoolCOp) condition;

		if (cond.isConstant()) {

			final CodePos pos = cond.getConstant() ? truePos : falsePos;

			if (pos != null) {
				go(pos);
			}

			return;
		}

		final CBlockPart prev = nextPart();

		resetNextPart();

		final CCodePos trueCPos = cast(truePos);
		final CCodePos actualTruePos;
		final CBlockPart nextTrue;

		if (trueCPos != null) {
			nextTrue = null;
			actualTruePos = trueCPos;
		} else {
			nextTrue = addNextPart();
			actualTruePos = nextTrue.head();
		}

		final CCodePos falseCPos = cast(falsePos);
		final CCodePos actualFalsePos;
		final CBlockPart nextFalse;

		if (falseCPos != null) {
			nextFalse = null;
			actualFalsePos = falseCPos;
		} else {
			if (nextTrue == null) {
				nextFalse = addNextPart();
			} else {
				nextFalse = nextTrue;
			}
			actualFalsePos = nextFalse.head();
		}

		if (actualTruePos == actualFalsePos) {
			new JumpBE.Unconditional(prev, actualTruePos);
			return;
		}

		new JumpBE.Conditional(prev, cond, actualTruePos, actualFalsePos);
	}

	@Override
	public final void returnVoid() {
		new ReturnBE(nextPart()) {
			@Override
			public void prepare() {
			}
			@Override
			protected void emit() {
				part().underlying().returnVoid();
			}
		};
	}

	protected final CBlockPart firstPart() {
		if (this.firstPart != null) {
			return this.firstPart;
		}

		this.firstPart = createFirstBlock();

		return this.nextPart = this.lastPart = this.firstPart;
	}

	protected abstract CBlockPart createFirstBlock();

	final void resetNextPart() {
		this.nextPart = null;
	}

	final void prepare() {
		if (!created()) {
			return;
		}
		this.firstPart.prepare();
		for (CBlock<?> subBlock : this.subBlocks) {
			subBlock.prepare();
		}
	}

	final void reveal() {
		if (!created()) {
			return;
		}
		this.firstPart.reveal();
		for (CBlock<?> subBlock : this.subBlocks) {
			subBlock.reveal();
		}
	}

	final void clear() {
		this.firstPart = null;
		this.lastPart = null;
		this.nextPart = null;
		this.subBlocks.clear();
	}

	private final CBlockPart addNextPart() {
		return this.nextPart = this.lastPart =
				this.lastPart.createNextPart(++this.blockSeq);
	}

	private static final class SubBlocks extends Chain<CBlock<?>> {

		@Override
		protected CBlock<?> next(CBlock<?> item) {
			return item.nextBlock;
		}

		@Override
		protected void setNext(CBlock<?> prev, CBlock<?> next) {
			prev.nextBlock = next;
		}

	}

}
