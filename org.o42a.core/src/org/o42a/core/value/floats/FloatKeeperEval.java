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
package org.o42a.core.value.floats;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.core.ir.object.state.FlaggedKeeperEval;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


final class FloatKeeperEval extends FlaggedKeeperEval<FloatKeeperIROp> {

	private final FloatKeeperIROp op;
	private Int64recOp value;

	FloatKeeperEval(KeeperOp<FloatKeeperIROp> keeper, FloatKeeperIROp op) {
		super(keeper);
		this.op = op;
	}

	@Override
	protected void start(Code code) {
		this.value =
				this.op.value(null, code)
				.toAny(null, code)
				.toInt64(null, code);
		super.start(code);
	}

	@Override
	protected Int8recOp flagsRec(Code code) {
		return this.op.flags(null, code);
	}

	@Override
	protected ValOp loadValue(ValDirs dirs, Code code) {
		return dirs.value().store(code, this.value.load(null, code));
	}

	@Override
	protected void updateValue(Code code, ValOp newValue) {
		this.value.store(
				code,
				newValue.rawValue(null, code).load(null, code),
				ATOMIC);
	}

}
