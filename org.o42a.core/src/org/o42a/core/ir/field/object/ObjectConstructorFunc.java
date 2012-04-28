/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.op.ObjectSignature;


public class ObjectConstructorFunc extends ObjectFunc<ObjectConstructorFunc> {

	public static final ObjectConstructor OBJECT_CONSTRUCTOR =
			new ObjectConstructor();

	private ObjectConstructorFunc(FuncCaller<ObjectConstructorFunc> caller) {
		super(caller);
	}

	public DataOp call(Code code, ObjectOp object, ObjFld.Op fld) {
		return invoke(
				null,
				code,
				OBJECT_CONSTRUCTOR.result(),
				object != null ? object.toData(null, code) : code.nullDataPtr(),
				fld);
	}

	public static final class ObjectConstructor
			extends ObjectSignature<ObjectConstructorFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;
		private Arg<ObjFld.Op> field;

		private ObjectConstructor() {
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<ObjFld.Op> field() {
			return this.field;
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectConstructorF");
		}

		@Override
		public ObjectConstructorFunc op(
				FuncCaller<ObjectConstructorFunc> caller) {
			return new ObjectConstructorFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
			this.field = builder.addPtr("field", ObjFld.OBJ_FLD);
		}

	}

}
