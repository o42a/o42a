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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectIRData.Op;


public final class FldCtrFinishFunc extends Func<FldCtrFinishFunc> {

	public static final FldCtrFinish FLD_CTR_FINISH = new FldCtrFinish();

	private FldCtrFinishFunc(FuncCaller<FldCtrFinishFunc> caller) {
		super(caller);
	}

	public final void call(Code code, ObjectIRData.Op data, FldCtrOp ctr) {
		invoke(null, code, FLD_CTR_FINISH.result(), data, ctr);
	}

	public static final class FldCtrFinish extends Signature<FldCtrFinishFunc> {

		private Return<Void> result;
		private Arg<Op> data;
		private Arg<FldCtrOp> ctr;

		private FldCtrFinish() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<Op> data() {
			return this.data;
		}

		public final Arg<FldCtrOp> ctr() {
			return this.ctr;
		}

		@Override
		public final FldCtrFinishFunc op(FuncCaller<FldCtrFinishFunc> caller) {
			return new FldCtrFinishFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("FldCtrFinishF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
			this.ctr = builder.addPtr("ctr", FLD_CTR_TYPE);
		}

	}

}
