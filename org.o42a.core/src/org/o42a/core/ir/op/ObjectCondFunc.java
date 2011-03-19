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
package org.o42a.core.ir.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;


public final class ObjectCondFunc extends ObjectFunc {

	public static final ObjectCond OBJECT_COND = new ObjectCond();

	ObjectCondFunc(FuncCaller<ObjectCondFunc> caller) {
		super(caller);
	}

	public BoolOp call(Code code, ObjectOp object) {
		return call(code, object.toData(code));
	}

	public BoolOp call(Code code, DataOp object) {
		return invoke(code, OBJECT_COND.result(), object);
	}

	public static final class ObjectCond
			extends ObjectSignature<ObjectCondFunc> {

		private Return<BoolOp> result;
		private Arg<DataOp> object;

		private ObjectCond() {
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		public ObjectCondFunc op(FuncCaller<ObjectCondFunc> caller) {
			return new ObjectCondFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectCondF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.object = builder.addData("object");
		}

	}

}
