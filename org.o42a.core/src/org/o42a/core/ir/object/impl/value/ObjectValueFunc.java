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
package org.o42a.core.ir.object.impl.value;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.op.ObjectSignature;


public final class ObjectValueFunc extends ObjectFunc<ObjectValueFunc> {

	public static final ObjectValue OBJECT_VALUE = new ObjectValue();

	private ObjectValueFunc(FuncCaller<ObjectValueFunc> caller) {
		super(caller);
	}

	public final void call(Code code, ObjectOp object) {
		call(
				code,
				object != null
				? object.objectType(code).ptr().data(code) : null,
				object);
	}

	public final void call(Code code, ObjectIRData.Op data, ObjectOp object) {
		invoke(
				null,
				code,
				OBJECT_VALUE.result(),
				data,
				object != null ? object.toData(null, code) : null);
	}

	public static final class ObjectValue
			extends ObjectSignature<ObjectValueFunc> {

		private Return<Void> result;
		private Arg<ObjectIRData.Op> data;
		private Arg<DataOp> object;

		private ObjectValue() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ObjectIRData.Op> data() {
			return this.data;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		public ObjectValueFunc op(FuncCaller<ObjectValueFunc> caller) {
			return new ObjectValueFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectValueF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
			this.object = builder.addData("object");
		}

	}

}
