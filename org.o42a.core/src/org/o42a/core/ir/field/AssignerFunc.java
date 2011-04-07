/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.op.ObjectSignature;


/**
 * Link or variable assignment function.
 */
public final class AssignerFunc extends ObjectFunc {

	public static Assigner ASSIGNER = new Assigner();

	AssignerFunc(FuncCaller<AssignerFunc> caller) {
		super(caller);
	}

	public BoolOp assign(Code code, DataOp object, DataOp value) {
		return invoke(null, code, ASSIGNER.result(), object, value);
	}

	public static final class Assigner
			extends ObjectSignature<AssignerFunc> {

		private Return<BoolOp> result;
		private Arg<DataOp> object;
		private Arg<DataOp> value;

		private Assigner() {
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<DataOp> value() {
			return this.value;
		}

		@Override
		public AssignerFunc op(FuncCaller<AssignerFunc> caller) {
			return new AssignerFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("AssignerF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.object = builder.addData("object");
			this.value = builder.addData("value");
		}

	}

}
