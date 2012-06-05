/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;


public final class ObjectRefFunc extends ObjectFunc<ObjectRefFunc> {

	public static final ObjectRef OBJECT_REF = new ObjectRef();

	ObjectRefFunc(FuncCaller<ObjectRefFunc> caller) {
		super(caller);
	}

	public DataOp call(Code code, ObjectOp object) {
		return invoke(
				null,
				code,
				OBJECT_REF.result(),
				object != null ? object.toData(null, code) : code.nullDataPtr());
	}

	public static final class ObjectRef
			extends ObjectSignature<ObjectRefFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;

		private ObjectRef() {
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectRefF");
		}

		@Override
		public ObjectRefFunc op(FuncCaller<ObjectRefFunc> caller) {
			return new ObjectRefFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
		}

	}

}
