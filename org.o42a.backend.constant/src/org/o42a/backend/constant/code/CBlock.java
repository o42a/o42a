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

	private CCodePos head;
	private CCodePos tail;

	public CBlock(
			ConstBackend backend,
			CFunction<?> function,
			B code,
			B underlying) {
		super(backend, function, code, underlying);
	}

	@Override
	public CodePos head() {

		final CodePos underlyingHead = getUnderlying().head();

		if (this.head != null && this.head.getUnderlying() == underlyingHead) {
			return this.head;
		}

		return this.head = new CCodePos(this, underlyingHead);
	}

	@Override
	public CodePos tail() {

		final CodePos underlyingTail = getUnderlying().tail();

		if (this.tail != null && this.tail.getUnderlying() == underlyingTail) {
			return this.tail;
		}

		return this.tail = new CCodePos(this, underlyingTail);
	}

	@Override
	public final void go(final CodePos pos) {
		new TermBE(this) {
			@Override
			protected void emit() {
				getUnderlying().go(cast(pos).getUnderlying());
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

		new TermBE(this) {
			@Override
			protected void emit() {
				cond.backend().underlying().go(
						block().getUnderlying(),
						cast(truePos).getUnderlying(),
						cast(falsePos).getUnderlying());
			}
		};
	}

	@Override
	public final void returnVoid() {
		beforeReturn();
		new TermBE(this) {
			@Override
			protected void emit() {
				block().getUnderlying().returnVoid();
			}
		};
	}

	public final void beforeReturn() {
		getFunction().getCallback().beforeReturn(code());
	}

	public final <T extends TermBE> T term(T op) {
		// TODO end block.
		return record(op);
	}

	@Override
	protected OpRecords records() {
		// TODO Auto-generated method stub
		return null;
	}

}
