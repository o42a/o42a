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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.object.ObjectIRData;


public final class ObjectDataCondFunc extends Func<ObjectDataCondFunc> {

	public static final ObjectDataCond OBJECT_DATA_COND = new ObjectDataCond();

	private ObjectDataCondFunc(FuncCaller<ObjectDataCondFunc> caller) {
		super(caller);
	}

	public final BoolOp call(Code code, ObjectIRData.Op data) {
		return invoke(null, code, OBJECT_DATA_COND.result(), data);
	}

	public static final class ObjectDataCond
			extends Signature<ObjectDataCondFunc> {

		private Return<BoolOp> result;
		private Arg<ObjectIRData.Op> data;

		private ObjectDataCond() {
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<ObjectIRData.Op> data() {
			return this.data;
		}

		@Override
		public final ObjectDataCondFunc op(FuncCaller<ObjectDataCondFunc> caller) {
			return new ObjectDataCondFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectDataCondF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

}
