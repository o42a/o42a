/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.util.string.ID;


public final class VariableUseFunc extends Func<VariableUseFunc> {

	public static final Signature VARIABLE_USE = new Signature();

	private VariableUseFunc(FuncCaller<VariableUseFunc> caller) {
		super(caller);
	}

	public DataOp use(Code code, DataRecOp var) {
		return invoke(null, code, VARIABLE_USE.result(), var.toAny(null, code));
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<VariableUseFunc> {

		private Return<DataOp> result;
		private Arg<AnyOp> var;

		private Signature() {
			super(ID.id("VariableUseF"));
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		public final Arg<AnyOp> var() {
			return this.var;
		}

		@Override
		public VariableUseFunc op(FuncCaller<VariableUseFunc> caller) {
			return new VariableUseFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.var = builder.addPtr("var");
		}

	}

}
