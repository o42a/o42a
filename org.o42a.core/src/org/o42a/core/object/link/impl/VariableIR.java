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
package org.o42a.core.object.link.impl;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.RMWKind.R_OR_W;
import static org.o42a.core.ir.field.variable.VarSte.varSteKey;
import static org.o42a.core.ir.value.Val.VAL_ASSIGN;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.field.variable.VarSte;
import org.o42a.core.ir.field.variable.VarSteOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.util.string.ID;


final class VariableIR extends ValueIR {

	private static final ID OLD_ID = ID.id("old");

	private ObjectIRBody bodyIR;
	private VarSte fld;

	VariableIR(VariableValueStructIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
		this.bodyIR = data.getBodyIR();
		this.fld = new VarSte();
		this.fld.declare(data);
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new VariableOp(this, object);
	}

	private static final class VariableOp extends ValueOp {

		VariableOp(VariableIR variableIR, ObjectOp object) {
			super(variableIR, object);
		}

		@Override
		public void init(Block code, ValOp value) {
			initValue(code, value);
		}

		@Override
		public void initToFalse(Block code) {
			initValue(code, null);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {

			final VarSteOp fld = (VarSteOp) object().field(
					dirs,
					varSteKey(getBuilder().getContext()));

			fld.value().assign(dirs, value);
		}

		@Override
		protected ValOp write(ValDirs dirs) {
			return defaultWrite(dirs);
		}

		@Override
		protected void checkFalse(
				Block code,
				CodePos falseDir,
				ValType.Op value,
				ValFlagsOp flags) {
			value.rawValue(null, code)
			.load(null, code)
			.eq(null, code, code.int64(0))
			.go(code, falseDir);
		}

		private void initValue(Block code, ValOp value) {

			final ValType.Op objectVal =
					object()
					.objectType(code)
					.ptr()
					.data(code).value(code);
			final ValFlagsOp flags = objectVal.flags(code, ACQUIRE_RELEASE);
			final Block skip = code.addBlock("skip");

			code.acquireBarrier();

			final ValFlagsOp old =
					flags.atomicRMW(OLD_ID, code, R_OR_W, VAL_ASSIGN);

			old.assigning(null, code).go(code, skip.head());

			if (value != null) {
				objectVal.rawValue(null, code).store(
						code,
						value.rawValue(null, code).load(null, code),
						ATOMIC);
			} else {
				objectVal.rawValue(null, code).store(
						code,
						code.int64(0),
						ATOMIC);
			}

			final Int32op newFlags =
					value != null ? value.flags(code).get() : code.int32(0);

			flags.store(code, newFlags);

			skip.go(code.tail());
		}

	}

}
