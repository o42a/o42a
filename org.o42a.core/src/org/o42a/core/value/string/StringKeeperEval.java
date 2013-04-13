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
package org.o42a.core.value.string;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.object.state.KeeperEval;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueType;


final class StringKeeperEval extends KeeperEval<StringKeeperIROp> {

	private final StringKeeperIROp op;
	private ValType.Op value;
	private ValFlagsOp flags;

	StringKeeperEval(KeeperOp<StringKeeperIROp> keeper, StringKeeperIROp op) {
		super(IndefIsFalse.INDEF_IS_FALSE, keeper);
		this.op = op;
	}

	@Override
	protected void start(Code code) {
		this.value = this.op.value(code);
		super.start(code);
		this.flags = this.value.flags(code, ATOMIC);
	}

	@Override
	protected BoolOp loadCondition(Code code) {
		return this.flags.condition(null, code);
	}

	@Override
	protected BoolOp loadIndefinite(Code code) {
		return this.flags.indefinite(null, code);
	}

	@Override
	protected ValOp loadValue(ValDirs dirs, Code code) {
		return this.value.op(
				code.getAllocator(),
				dirs.getBuilder(),
				ValueType.STRING,
				TEMP_VAL_HOLDER);
	}

	@Override
	protected void updateValue(Code code, ValOp newValue) {
		this.value.length(null, code).store(
				code,
				newValue.length(null, code).load(null, code),
				ATOMIC);
		this.value.rawValue(null, code).store(
				code,
				newValue.rawValue(null, code).load(null, code),
				ATOMIC);

		code.releaseBarrier();

		this.flags.store(code, newValue.flags(code).get());
		this.value.useRefCounted(code);
	}

	@Override
	protected void updateCondition(Code code, boolean condition) {
		if (condition) {
			return;
		}
		code.releaseBarrier();
		this.flags.storeFalse(code);
	}

}
