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
package org.o42a.core.ir.object.op;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.op.CtrOp.Op;
import org.o42a.util.string.ID;


public class NewObjectFunc extends Func<NewObjectFunc> {

	public static final Signature NEW_OBJECT = new Signature();

	private NewObjectFunc(FuncCaller<NewObjectFunc> caller) {
		super(caller);
	}

	public DataOp newObject(Code code, CtrOp ctr) {
		return invoke(null, code, NEW_OBJECT.result(), ctr.ptr());
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<NewObjectFunc> {

		private Return<DataOp> result;
		private Arg<Op> ctr;

		private Signature() {
			super(ID.id("NewObjectF"));
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		public final Arg<Op> ctr() {
			return this.ctr;
		}

		@Override
		public NewObjectFunc op(FuncCaller<NewObjectFunc> caller) {
			return new NewObjectFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.ctr = builder.addPtr("ctr", CtrOp.CTR_TYPE);
		}

	}

}
