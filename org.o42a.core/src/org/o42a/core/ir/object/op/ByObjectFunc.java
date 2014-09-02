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
package org.o42a.core.ir.object.op;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.util.string.ID;


public final class ByObjectFunc extends Func<ByObjectFunc> {

	public static final Signature BY_OBJECT = new Signature();

	private ByObjectFunc(FuncCaller<ByObjectFunc> caller) {
		super(caller);
	}

	public final void call(Code code, DataOp object) {
		invoke(null, code, BY_OBJECT.result(), object);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ByObjectFunc> {

		private Return<Void> result;
		private Arg<DataOp> object;

		private Signature() {
			super(ID.id("ObjectDataF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		public final ByObjectFunc op(FuncCaller<ByObjectFunc> caller) {
			return new ByObjectFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.object = builder.addData("object");
		}

	}

}
