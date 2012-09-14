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
package org.o42a.core.value.floats;

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperEval;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


final class FloatKeeperIROp extends KeeperIROp<FloatKeeperIROp> {

	FloatKeeperIROp(StructWriter<FloatKeeperIROp> writer) {
		super(writer);
	}

	@Override
	public final FloatKeeperIRType getType() {
		return (FloatKeeperIRType) super.getType();
	}

	public final Int8recOp flags(ID id, Code code) {
		return int8(id, code, getType().flags());
	}

	public final Fp64recOp value(ID id, Code code) {
		return fp64(id, code, getType().value());
	}

	@Override
	protected void writeCond(KeeperOp keeper, CodeDirs dirs) {
		new Eval(dirs.code(), keeper, this).writeCond(dirs);
	}

	@Override
	protected ValOp writeValue(KeeperOp keeper, ValDirs dirs) {
		return new Eval(dirs.code(), keeper, this).writeValue(dirs);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp keeper,
			CodeDirs dirs,
			ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	private static final class Eval extends KeeperEval {

		private final Int8recOp flagsRec;
		private final Int8op flags;
		private final Int64recOp value;

		Eval(Code code, KeeperOp keeper, FloatKeeperIROp op) {
			super(keeper);
			this.flagsRec = op.flags(null, code);
			this.flags = this.flagsRec.load(null, code, ATOMIC);
			// Floats can not be stored atomically, so change to int64.
			this.value =
					op.value(null, code)
					.toAny(null, code)
					.toInt64(null, code);
		}

		@Override
		protected BoolOp loadCondition(Code code) {
			return this.flags.lowestBit(null, code);
		}

		@Override
		protected BoolOp loadIndefinite(Code code) {
			return this.flags
					.lshr(null, code, numberOfTrailingZeros(VAL_INDEFINITE))
					.lowestBit(null, code);
		}

		@Override
		protected ValOp loadValue(ValDirs dirs, Code code) {
			return dirs.value().store(code, this.value.load(null, code));
		}

		@Override
		protected void storeValue(Code code, ValOp newValue) {
			this.value.store(
					code,
					newValue.rawValue(null, code).load(null, code),
					ATOMIC);
		}

		@Override
		protected void storeCondition(Code code, boolean condition) {
			this.flagsRec.store(
					code,
					code.int8(condition ? (byte) VAL_CONDITION : 0),
					ATOMIC);
		}

	}

}
