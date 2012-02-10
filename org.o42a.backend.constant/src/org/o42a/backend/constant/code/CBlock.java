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
import org.o42a.backend.constant.code.op.TermBE;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
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
		return this.nextPart != null && this.nextPart.exists();
	}

	@Override
	public CodePos head() {
		return firstPart().head();
	}

	@Override
	public CodePos tail() {
		if (this.nextPart != null && !this.nextPart.isEmpty()) {
			this.nextPart = null;
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

		this.nextPart = this.lastPart =
				this.lastPart.createNextPart(++this.blockSeq);

		if (!prev.isTerminated()) {

			final CCodePos nextHead = this.nextPart.head().comeFrom(prev);

			new TermBE(prev) {
				@Override
				protected void emit() {

					final CBlockPart part = (CBlockPart) part();

					part.underlying().go(nextHead.getUnderlying());
				}
			};
		}

		return this.nextPart;
	}

	@Override
	public final CCodeBlock block(Block code) {
		return this.subBlocks.add(new CCodeBlock(this, code));
	}

	@Override
	public final void go(final CodePos pos) {

		final CCodePos cpos = cast(pos).comeFrom(this);

		new TermBE(this) {
			@Override
			protected void emit() {
				part().underlying().go(cpos.getUnderlying());
			}
		};
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

		final CCodePos trueCPos = cast(truePos).comeFrom(this);
		final CCodePos falseCPos = cast(falsePos).comeFrom(this);

		new TermBE(this) {
			@Override
			protected void emit() {
				cond.backend().underlying().go(
						part().underlying(),
						trueCPos.getUnderlying(),
						falseCPos.getUnderlying());
			}
		};
	}

	@Override
	public final void returnVoid() {
		beforeReturn();
		new TermBE(this) {
			@Override
			protected void emit() {
				part().underlying().returnVoid();
			}
		};
	}

	public final void beforeReturn() {
		getFunction().getCallback().beforeReturn(code());
	}

	@SuppressWarnings("unchecked")
	public final CCodePart<Block> term(TermBE op) {

		final CCodePart<Block> part = (CCodePart<Block>) record(op);

		this.lastPart.terminate();
		this.nextPart = null;

		return part;
	}

	protected final CBlockPart firstPart() {
		if (this.firstPart != null) {
			return this.firstPart;
		}

		this.firstPart = createFirstBlock();

		return this.nextPart = this.lastPart = this.firstPart;
	}

	protected abstract CBlockPart createFirstBlock();

	final void initUnderlying(Block underlyingEnclosing) {
		if (this.firstPart == null) {
			return;
		}
		this.firstPart.initUnderlying(underlyingEnclosing);

		final Block underlying = this.firstPart.underlying();

		for (CBlock<?> subBlock : this.subBlocks) {
			subBlock.initUnderlying(underlying);
		}
	}

	final void reveal() {
		if (this.firstPart == null) {
			return;
		}
		this.firstPart.reveal();
		for (CBlock<?> subBlock : this.subBlocks) {
			subBlock.reveal();
		}
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
