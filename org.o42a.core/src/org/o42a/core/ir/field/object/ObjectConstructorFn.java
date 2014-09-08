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
import static org.o42a.core.ir.object.op.CtrOp.CTR_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.VmtIRChain;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.util.string.ID;


public final class ObjectConstructorFn
		extends ObjectFn<ObjectConstructorFn> {

	public static final Signature OBJECT_CONSTRUCTOR = new Signature();

	private ObjectConstructorFn(FuncCaller<ObjectConstructorFn> caller) {
		super(caller);
	}

	public final DataOp call(Code code, VmtIRChain.Op vmtc, CtrOp ctr) {
		return call(code, vmtc, ctr.ptr(code));
	}

	public final DataOp call(Code code, VmtIRChain.Op vmtc, CtrOp.Op ctr) {
		return invoke(null, code, OBJECT_CONSTRUCTOR.result(), vmtc, ctr);
	}

	public static final class Signature
			extends ObjectSignature<ObjectConstructorFn> {

		private Return<DataOp> result;
		private Arg<VmtIRChain.Op> vmtc;
		private Arg<CtrOp.Op> ctr;

		private Signature() {
			super(ID.rawId("ObjectConstructorF"));
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		public final Arg<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		public final Arg<CtrOp.Op> ctr() {
			return this.ctr;
		}

		@Override
		public ObjectConstructorFn op(FuncCaller<ObjectConstructorFn> caller) {
			return new ObjectConstructorFn(caller);
		}

		@Override
		public DataOp object(Code code, Function<?> function) {
			return function.arg(code, ctr()).owner(code).load(null, code);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.vmtc = builder.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
			this.ctr = builder.addPtr("ctr", CTR_TYPE);
		}

	}

}
