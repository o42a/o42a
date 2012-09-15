/*
    Compiler Core
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
package org.o42a.core.value.voids;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.core.ir.object.state.FlaggedKeeperEval;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


final class VoidKeeperEval extends FlaggedKeeperEval {

	private final VoidKeeperIROp op;

	VoidKeeperEval(KeeperOp keeper, VoidKeeperIROp op) {
		super(keeper);
		this.op = op;
	}

	@Override
	protected Int8recOp flagsRec(Code code) {
		return this.op.flags(null, code);
	}

	@Override
	protected ValOp loadValue(ValDirs dirs, Code code) {
		return voidValue().op(dirs.getBuilder(), code);
	}

	@Override
	protected void updateValue(Code code, ValOp newValue) {
	}

	@Override
	protected void updateCondition(Code code, boolean condition) {
		flagsRec().store(
				code,
				code.int8(condition ? (byte) VAL_CONDITION : 0),
				ACQUIRE_RELEASE);
	}

}
