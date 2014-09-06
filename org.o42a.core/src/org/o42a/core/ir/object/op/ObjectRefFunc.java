/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.VmtIRChain;
import org.o42a.util.string.ID;


public final class ObjectRefFunc extends ObjectFunc<ObjectRefFunc> {

	public static final Signature OBJECT_REF = new Signature();

	ObjectRefFunc(FuncCaller<ObjectRefFunc> caller) {
		super(caller);
	}

	public DataOp call(
			Code code,
			ObjectOp object,
			VmtIRChain.Op vmtc) {
		return invoke(
				null,
				code,
				OBJECT_REF.result(),
				object != null
				? object.toData(null, code)
				: code.nullDataPtr(),
				vmtc != null ? vmtc : code.nullPtr(VMT_IR_CHAIN_TYPE));
	}

	public static final class Signature
			extends ObjectArgSignature<ObjectRefFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;
		private Arg<VmtIRChain.Op> vmtc;

		private Signature() {
			super(ID.id("ObjectRefF"));
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		@Override
		public ObjectRefFunc op(FuncCaller<ObjectRefFunc> caller) {
			return new ObjectRefFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
			this.vmtc = builder.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
		}

	}

}
