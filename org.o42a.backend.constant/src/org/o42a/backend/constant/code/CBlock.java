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


public abstract class CBlock<B extends Block> extends CCode<B>
		implements BlockWriter {

	private CBlockPart firstPart;
	private CBlockPart lastPart;
	private CBlockPart nextPart;
	private int blockSeq;

	public CBlock(ConstBackend backend, CFunction<?> function, B code) {
		super(backend, function, code);
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
		return this.nextPart = this.lastPart.createNextPart(
				getId().anonymous(++this.blockSeq));
	}

	@Override
	public final void go(final CodePos pos) {

		final CCodePos cpos = cast(pos);

		new TermBE(this) {
			@Override
			protected void emit() {
				part().underlying().go(cpos.getUnderlying());
			}
		};

		cpos.part().comeFrom(this);
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

		final CCodePos trueCPos = cast(truePos);
		final CCodePos falseCPos = cast(falsePos);

		new TermBE(this) {
			@Override
			protected void emit() {
				cond.backend().underlying().go(
						part().underlying(),
						trueCPos.getUnderlying(),
						falseCPos.getUnderlying());
			}
		};

		trueCPos.comeFrom(this);
		falseCPos.comeFrom(this);
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
		this.nextPart = null;
		return (CCodePart<Block>) record(op);
	}

	protected final CBlockPart firstPart() {
		if (this.firstPart != null) {
			return this.firstPart;
		}

		this.firstPart = createFirstBlock();

		return this.nextPart = this.lastPart = this.firstPart;
	}

	protected abstract CBlockPart createFirstBlock();

}
