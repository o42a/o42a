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

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.RMWKind.R_OR_W;
import static org.o42a.core.ir.field.variable.AssignerFld.assignerKey;
import static org.o42a.core.ir.value.Val.ASSIGN_FLAG;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.variable.AssignerFld;
import org.o42a.core.ir.field.variable.AssignerFldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;


final class VariableIR extends ValueIR {

	private ObjectBodyIR bodyIR;
	private AssignerFld fld;

	VariableIR(VariableValueStructIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public Fld allocateBody(ObjectBodyIR bodyIR, SubData<?> data) {
		this.bodyIR = bodyIR;
		this.fld = new AssignerFld(bodyIR);
		this.fld.declare(data);
		return this.fld;
	}

	@Override
	public void allocateMethods(ObjectMethodsIR methodsIR, SubData<?> data) {
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

			final AssignerFldOp fld = (AssignerFldOp) object().field(
					dirs,
					assignerKey(getBuilder().getContext()));

			fld.assign(dirs, value);
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
			value.rawValue(null, code).isNull(null, code).go(code, falseDir);
		}

		private void initValue(Block code, ValOp value) {

			final ValType.Op objectVal =
					object()
					.objectType(code)
					.ptr()
					.data(code).value(code);
			final ValFlagsOp flags = objectVal.flags(code, ATOMIC);
			final Block skip = code.addBlock("skip");

			code.acquireBarrier();

			final ValFlagsOp old = flags.atomicRMW(
					code.id("old"),
					code,
					R_OR_W,
					ASSIGN_FLAG);

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

			code.releaseBarrier();

			flags.store(code, newFlags);

			skip.go(code.tail());
		}

	}

}
