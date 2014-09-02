/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.VmtIRChain;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.util.string.ID;


public final class ObjectConstructorFunc
		extends ObjectFunc<ObjectConstructorFunc> {

	public static final Signature OBJECT_CONSTRUCTOR = new Signature();

	private ObjectConstructorFunc(FuncCaller<ObjectConstructorFunc> caller) {
		super(caller);
	}

	public final DataOp call(Code code, ObjectOp object, VmtIRChain.Op vmtc) {
		return call(code, object, vmtc, (DataOp) null);
	}

	public final DataOp call(
			Code code,
			ObjectOp object,
			VmtIRChain.Op vmtc,
			ObjectOp ancestor) {
		return call(
				code,
				object,
				vmtc,
				ancestor != null ? ancestor.toData(null, code) : null);
	}

	public final DataOp call(
			Code code,
			ObjectOp object,
			VmtIRChain.Op vmtc,
			DataOp ancestor) {
		return invoke(
				null,
				code,
				OBJECT_CONSTRUCTOR.result(),
				object != null
				? object.toData(null, code) : code.nullDataPtr(),
				vmtc != null ? vmtc : code.nullPtr(VMT_IR_CHAIN_TYPE),
				ancestor != null ? ancestor : code.nullDataPtr());
	}

	public static final class Signature
			extends ObjectSignature<ObjectConstructorFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;
		private Arg<VmtIRChain.Op> vmtc;
		private Arg<DataOp> ancestor;

		private Signature() {
			super(ID.id("ObjectConstructorF"));
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

		public final Arg<DataOp> ancestor() {
			return this.ancestor;
		}

		@Override
		public ObjectConstructorFunc op(
				FuncCaller<ObjectConstructorFunc> caller) {
			return new ObjectConstructorFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
			this.vmtc = builder.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
			this.ancestor = builder.addData("ancestor");
		}

	}

}
