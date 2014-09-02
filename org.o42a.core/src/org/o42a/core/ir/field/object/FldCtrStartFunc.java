/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.util.string.ID;


public final class FldCtrStartFunc extends Func<FldCtrStartFunc> {

	public static final Signature FLD_CTR_START = new Signature();

	private FldCtrStartFunc(FuncCaller<FldCtrStartFunc> caller) {
		super(caller);
	}

	public final BoolOp call(Code code, DataOp object, FldCtrOp ctr) {
		return invoke(null, code, FLD_CTR_START.result(), object, ctr);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<FldCtrStartFunc> {

		private Return<BoolOp> result;
		private Arg<DataOp> object;
		private Arg<FldCtrOp> ctr;

		private Signature() {
			super(ID.id("FldCtrStartF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<FldCtrOp> ctr() {
			return this.ctr;
		}

		@Override
		public final FldCtrStartFunc op(FuncCaller<FldCtrStartFunc> caller) {
			return new FldCtrStartFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.object = builder.addData("object");
			this.ctr = builder.addPtr("ctr", FLD_CTR_TYPE);
		}

	}

}
