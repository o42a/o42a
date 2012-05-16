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

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.RMWKind.R_OR_W;
import static org.o42a.core.ir.field.variable.AssignerFld.assignerKey;
import static org.o42a.core.ir.value.Val.ASSIGN_FLAG;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int32recOp;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.variable.AssignerFld;
import org.o42a.core.ir.field.variable.AssignerFldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
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

			final ValType.Op objectVal =
					object()
					.objectType(code)
					.ptr()
					.data(code).value(code);
			final Int32recOp flags = objectVal.flags(null, code);
			final Block skip = code.addBlock("skip");

			code.acquireBarrier();

			final Int32op old = flags.atomicRMW(
					code.id("old"),
					code,
					R_OR_W,
					code.int32(ASSIGN_FLAG));

			old.lshr(
					code.id("uassign_in_proc"),
					code,
					numberOfTrailingZeros(ASSIGN_FLAG))
			.lowestBit(code.id("assign_in_proc"), code)
			.go(code, skip.head());

			defaultInit(code, value);

			skip.go(code.tail());
		}

		@Override
		public void initToFalse(Block code) {
			defaultInitToFalse(code);
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

	}

}
