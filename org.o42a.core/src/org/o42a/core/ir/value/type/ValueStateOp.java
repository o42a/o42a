/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.ir.value.type;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueType;


public abstract class ValueStateOp extends StateOp {

	private ValType.Op value;
	private ValFlagsOp flags;

	public ValueStateOp(ObjectOp host) {
		super(host);
	}

	@Override
	public ValueType<?> getValueType() {
		return host().getWellKnownType().type().getValueType();
	}

	public final ValType.Op value() {
		return this.value;
	}

	public final ValFlagsOp flags() {
		return this.flags;
	}

	@Override
	public void startEval(Block code, CodePos failure, FldCtrOp ctr) {

		final ObjectIRDataOp data = host().objectType(code).ptr().data(code);

		this.value = data.value(code);
		this.flags = this.value.flags(code, ATOMIC);
		ctr.start(code, data).goUnless(code, failure);
	}

	@Override
	public BoolOp loadCondition(Code code) {
		return this.flags.condition(null, code);
	}

	@Override
	public ValOp loadValue(ValDirs dirs, Code code) {
		return dirs.value().store(
				code,
				this.value.op(
						code.getAllocator(),
						dirs.getBuilder(),
						getValueType(),
						TEMP_VAL_HOLDER));
	}

	@Override
	public void initToFalse(Block code) {
		code.releaseBarrier();
		this.flags.storeFalse(code);
	}

	@Override
	protected BoolOp loadIndefinite(Code code) {
		return this.flags.indefinite(null, code);
	}

	@Override
	protected void start(Block code) {

		final ObjectIRDataOp data = host().objectType(code).ptr().data(code);

		this.value = data.value(code);
		super.start(code);
		this.flags = this.value.flags(code, ATOMIC);
	}

}
