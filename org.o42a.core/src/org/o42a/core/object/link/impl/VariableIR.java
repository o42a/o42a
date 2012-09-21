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
import static org.o42a.core.ir.field.variable.VarSte.varSteKey;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.field.variable.VarSte;
import org.o42a.core.ir.field.variable.VarSteOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.*;


final class VariableIR extends ValueIR {

	VariableIR(VariableValueStructIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
		new VarSte().declare(data);
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new VariableOp(this, object);
	}

	private static final class VariableOp extends StatefulValueOp {

		VariableOp(VariableIR variableIR, ObjectOp object) {
			super(variableIR, object);
		}

		@Override
		public StateOp state(CodeDirs dirs) {

			final VarSteOp fld = (VarSteOp) object().field(
					dirs,
					varSteKey(getBuilder().getContext()));

			return new VarStateOp(fld);
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

	}

	private static final class VarStateOp extends StateOp {

		private DataRecOp objectRec;
		private DataOp objectPtr;

		VarStateOp(VarSteOp fld) {
			super(fld);
		}

		public final VarSteOp var() {
			return (VarSteOp) fld();
		}

		@Override
		public void useByValueFunction(Code code) {
			this.objectRec = var().ptr().object(null, code);
			this.objectPtr = this.objectRec.load(null, code, ATOMIC);
		}

		@Override
		public BoolOp loadCondition(Code code) {
			return this.objectPtr.ne(null, code, none(code));
		}

		@Override
		public ValOp loadValue(ValDirs dirs, Code code) {
			return dirs.value().store(code, this.objectPtr.toAny(null, code));
		}

		@Override
		public void init(Block code, ValOp value) {

			final DataOp target =
					value.value(null, code)
					.toPtr(null, code)
					.load(null, code)
					.toData(null, code);

			this.objectRec.store(code, target, ACQUIRE_RELEASE);
		}

		@Override
		public void initToFalse(Block code) {
			this.objectRec.store(code, none(code), ACQUIRE_RELEASE);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			var().value().assign(dirs, value);
		}

		@Override
		protected void start(Block code) {
			this.objectRec = var().ptr().object(null, code);
			super.start(code);
			this.objectPtr = this.objectRec.load(null, code, ATOMIC);
		}

		@Override
		protected BoolOp loadIndefinite(Code code) {
			return this.objectPtr.isNull(null, code);
		}

		private DataOp none(Code code) {

			final ObjectIR noneIR = getContext().getNone().ir(getGenerator());
			final Ptr<ObjectIRBodyOp> nonePtr =
					noneIR.getMainBodyIR().pointer(getGenerator());

			return nonePtr.op(null, code).toData(null, code);
		}

	}

}
