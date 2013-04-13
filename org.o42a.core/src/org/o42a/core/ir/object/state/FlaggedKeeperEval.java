/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.object.state;

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int8op;
import org.o42a.codegen.code.op.Int8recOp;


public abstract class FlaggedKeeperEval<O extends KeeperIROp<O>>
		extends KeeperEval<O> {

	private Int8recOp flagsRec;
	private Int8op flags;

	public FlaggedKeeperEval(KeeperOp<O> keeper) {
		super(IndefIsFalse.INDEF_IS_FALSE, keeper);
	}

	protected final Int8op flags() {
		return this.flags;
	}

	protected final Int8recOp flagsRec() {
		return this.flagsRec;
	}

	@Override
	protected void start(Code code) {
		this.flagsRec = flagsRec(code);
		super.start(code);
		this.flags = this.flagsRec.load(null, code, ATOMIC);
	}

	@Override
	protected final BoolOp loadCondition(Code code) {
		return flags().lowestBit(null, code);
	}

	@Override
	protected final BoolOp loadIndefinite(Code code) {
		return flags()
				.lshr(null, code, numberOfTrailingZeros(VAL_INDEFINITE))
				.lowestBit(null, code);
	}

	@Override
	protected void updateCondition(Code code, boolean condition) {
		code.releaseBarrier();
		this.flagsRec.store(
				code,
				code.int8(condition ? (byte) VAL_CONDITION : 0),
				ATOMIC);
	}

	protected abstract Int8recOp flagsRec(Code code);

}
