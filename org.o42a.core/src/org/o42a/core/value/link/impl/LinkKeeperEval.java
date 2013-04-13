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
package org.o42a.core.value.link.impl;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.state.KeeperEval;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


final class LinkKeeperEval extends KeeperEval<LinkKeeperIROp> {

	private final LinkKeeperIROp op;
	private DataRecOp objectRec;
	private DataOp object;

	LinkKeeperEval(KeeperOp<LinkKeeperIROp> keeper, LinkKeeperIROp op) {
		super(IndefIsFalse.INDEF_NOT_FALSE, keeper);
		this.op = op;
	}

	@Override
	protected void start(Code code) {
		this.objectRec = this.op.object(code);
		super.start(code);
		this.object = this.objectRec.load(null, code, ATOMIC);
	}

	@Override
	protected BoolOp loadCondition(Code code) {
		return this.object.ne(ID.id("not_none"), code, none(code));
	}

	@Override
	protected BoolOp loadIndefinite(Code code) {
		return this.object.isNull(null, code);
	}

	@Override
	protected ValOp loadValue(ValDirs dirs, Code code) {
		return dirs.value().store(code, this.object.toAny(null, code));
	}

	@Override
	protected void updateValue(Code code, ValOp newValue) {

		final DataOp newObjectPtr =
				newValue.value(null, code)
				.toRec(null, code)
				.load(null, code)
				.toData(null, code);

		this.objectRec.store(code, newObjectPtr, ACQUIRE_RELEASE);
	}

	@Override
	protected void updateCondition(Code code, boolean condition) {
		if (condition) {
			return;
		}
		this.objectRec.store(code, none(code), ACQUIRE_RELEASE);
	}

	private DataOp none(Code code) {

		final Obj none = getContext().getNone();
		final ObjectIR noneIR = none.ir(getGenerator());
		final Ptr<DataOp> nonePtr =
				noneIR.getMainBodyIR().pointer(getGenerator()).toData();

		return nonePtr.op(null, code);
	}

}
