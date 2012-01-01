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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.op.ObjectSignature;
import org.o42a.core.ir.op.ValDirs;


public final class ObjectValFunc extends ObjectFunc<ObjectValFunc> {

	public static final ObjectVal OBJECT_VAL = new ObjectVal();

	private ObjectValFunc(FuncCaller<ObjectValFunc> caller) {
		super(caller);
	}

	public void call(Code code, ValOp value, ObjectOp object) {
		call(code, value, object.toData(code));
	}

	public ValOp call(ValDirs dirs, ObjectOp object) {
		return call(dirs, object.toData(dirs.code()));
	}

	public void call(Code code, ValOp value, DataOp object) {
		invoke(null, code, OBJECT_VAL.result(), value.ptr(), object);
	}

	public ValOp call(ValDirs dirs, DataOp object) {

		final Code code = dirs.code();
		final ValOp value = dirs.value();

		invoke(null, code, OBJECT_VAL.result(), value.ptr(), object);
		value.go(code, dirs);

		return value;
	}

	public static final class ObjectVal extends ObjectSignature<ObjectValFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> value;
		private Arg<DataOp> object;

		private ObjectVal() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> value() {
			return this.value;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		public ObjectValFunc op(FuncCaller<ObjectValFunc> caller) {
			return new ObjectValFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectValF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.value = builder.addPtr("value", VAL_TYPE);
			this.object = builder.addData("object");
		}

	}

}
