/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.CodeOp;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Chain;


public abstract class CBlock<B extends Block> extends CCode<B>
		implements BlockWriter {

	private CBlockPart firstPart;
	private CBlockPart lastPart;
	private CBlockPart nextPart;

	private final Chain<CBlock<?>> subBlocks =
			new Chain<>(CBlock::getNextBlock, CBlock::setNextBlock);
	private StartAllocation startAllocation;
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
	public CCodePos tail() {
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
	public AllocatorWriter startAllocation(Allocator allocator) {

		final StartAllocation initAllocation =
				new StartAllocation(this, allocator);

		if (allocator != null) {

			final CBlock<?> allocBlock = cast(allocator.writer());

			allocBlock.startAllocation = initAllocation;
		} else {
			initAllocation.alwaysEmit();
		}

		return new CAllocator(initAllocation);
	}

	@Override
	public final void go(final CodePos pos) {
		new JumpBE.Unconditional(nextPart(), cast(pos));
		resetNextPart();
	}

	@Override
	public void go(CodeOp pos, CodePos[] targets) {

		final CodeCOp cpos = (CodeCOp) pos;
		final CBlockPart prev = nextPart();

		resetNextPart();

		final CCodePos poss[] = new CCodePos[targets.length];

		for (int i = 0; i < targets.length; ++i) {
			poss[i] = cast(targets[i]);
		}

		new JumpBE.Potential(prev, cpos, poss);
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
	public final void returnVoid(boolean dispose) {
		new ReturnBE(nextPart(), dispose) {
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

	final void allocate(AllocPtrCOp<?> op) {
		if (this.startAllocation != null) {
			this.startAllocation.useBy(op);
		}
	}

	final void resetNextPart() {
		this.nextPart = null;
	}

	final void prepare() {
		if (!created()) {
			return;
		}
		this.firstPart.prepareAll();
		for (CBlock<?> subBlock : this.subBlocks) {
			subBlock.prepare();
		}
	}

	final void reveal() {
		if (!created()) {
			return;
		}
		this.firstPart.revealAll();
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

	private CBlock<?> getNextBlock() {
		return this.nextBlock;
	}

	private void setNextBlock(CBlock<?> nextBlock) {
		this.nextBlock = nextBlock;
	}

	private static final class StartAllocation extends BaseInstrBE {

		private final Allocator allocator;
		private AllocatorWriter writer;

		StartAllocation(CBlock<?> code, Allocator allocator) {
			super(code);
			this.allocator = allocator;
		}

		@Override
		public void prepare() {
		}

		@Override
		public String toString() {
			if (this.allocator == null) {
				return super.toString();
			}
			return "StartAllocation[" + this.allocator + ']';
		}

		@Override
		protected void emit() {

			final CBlockPart part = (CBlockPart) part();

			this.writer = part.underlying().writer().startAllocation(null);
		}

		final AllocatorWriter writer() {
			if (this.writer != null) {
				return this.writer;
			}
			part().revealUpTo(this);
			assert this.writer != null :
				"Can not start allocation";
			return this.writer;
		}

	}

	private static final class AllocateUnderlying extends BaseInstrBE {

		private final CodePos target;
		private final StartAllocation startAllocation;
		private boolean revealed;

		AllocateUnderlying(
				CCode<?> code,
				CodePos target,
				StartAllocation initAllocation) {
			super(code);
			this.target = target;
			this.startAllocation = initAllocation;
		}

		@Override
		public void prepare() {
			this.startAllocation.use(this);
		}

		@Override
		public String toString() {
			if (this.startAllocation == null) {
				return super.toString();
			}
			return "AllocateUnderlying[" + this.startAllocation.allocator + ']';
		}

		@Override
		protected void emit() {
			this.revealed = true;
			this.startAllocation.writer().allocate(
					part().underlying(),
					this.target);
		}

		final AllocatorWriter allocate() {
			if (!this.revealed) {
				part().revealUpTo(this);
				assert this.revealed :
					"Can not reallocate underlying allocator";
			}
			return this.startAllocation.writer();
		}

	}

	private static final class CombineUnderlying extends BaseInstrBE {

		private final Code originalCode;
		private final CAllocator allocator;
		private final StartAllocation startAllocation;
		private boolean revealed;

		CombineUnderlying(
				CCode<?> code,
				Code originalCode,
				CAllocator allocator) {
			super(code);
			this.originalCode = originalCode;
			this.allocator = allocator;
			this.startAllocation = allocator.startAllocation;
		}

		@Override
		public void prepare() {
			this.startAllocation.use(this);
		}

		@Override
		public String toString() {
			if (this.startAllocation == null) {
				return super.toString();
			}
			return "CombineUnderlying[" + this.startAllocation.allocator + ']';
		}

		@Override
		protected void emit() {
			this.revealed = true;
			this.allocator.emitAllocs();
			this.startAllocation.writer().combine(
					part().underlying(),
					this.originalCode);
		}

		final AllocatorWriter combine() {
			if (!this.revealed) {
				part().revealUpTo(this);
				assert this.revealed :
					"Can not allocate underlying allocator";
			}
			return this.startAllocation.writer();
		}

	}

	private static final class CAllocator implements AllocatorWriter {

		private final StartAllocation startAllocation;
		private AllocateUnderlying[] allocs;
		private CombineUnderlying combine;

		CAllocator(StartAllocation initAllocation) {
			this.startAllocation = initAllocation;
		}

		@Override
		public void allocate(Code code, CodePos target) {

			final AllocateUnderlying alloc = new AllocateUnderlying(
					cast(code),
					target,
					this.startAllocation);

			if (this.allocs == null) {
				this.allocs = new AllocateUnderlying[] {alloc};
			} else {
				this.allocs = ArrayUtil.append(this.allocs, alloc);
			}
		}

		@Override
		public void combine(Code code, Code originalCode) {
			this.combine = new CombineUnderlying(
					cast(code),
					originalCode,
					this);
		}

		@Override
		public void dispose(Code code, final Code originalCode) {
			new BaseInstrBE(cast(code)) {
				@Override
				public void prepare() {
					useBy(CAllocator.this.combine);
				}
				@Override
				protected void emit() {
					emitDispose(part(), originalCode);
				}
			};
		}

		@Override
		public String toString() {
			if (this.startAllocation == null) {
				return super.toString();
			}
			return "CAllocator[" + this.startAllocation.allocator + ']';
		}

		void emitAllocs() {
			if (this.allocs != null) {
				for (AllocateUnderlying alloc : this.allocs) {
					alloc.allocate();
				}
			}
		}

		private void emitDispose(CCodePart<?> part, Code originalCode) {
			this.combine.combine().dispose(part.underlying(), originalCode);
		}

	}

}
