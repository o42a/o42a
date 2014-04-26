/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.util.string.ID;


public final class ObjectValFunc extends ObjectFunc<ObjectValFunc> {

	public static final Signature OBJECT_VAL = new Signature();

	private ObjectValFunc(FuncCaller<ObjectValFunc> caller) {
		super(caller);
	}

	public void call(DefDirs dirs, ObjectOp object) {
		call(dirs, object.toData(null, dirs.code()));
	}

	public void call(DefDirs dirs, DataOp object) {

		final Block code = dirs.code();
		final ValOp value = dirs.value();

		invoke(null, code, OBJECT_VAL.result(), value.ptr(code), object);

		final Block hasResult = code.addBlock("has_result");

		final BoolOp condition = value.flags(code).condition(null, code);

		condition.go(code, hasResult.head());
		value.holder().set(hasResult);
		dirs.returnValue(hasResult, value);

		value.flags(code)
		.indefinite(null, code)
		.goUnless(code, dirs.falseDir());
	}

	public static final class Signature extends ObjectSignature<ObjectValFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> value;
		private Arg<DataOp> object;

		private Signature() {
			super(ID.id("ObjectValF"));
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
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.value = builder.addPtr("value", VAL_TYPE);
			this.object = builder.addData("object");
		}

	}

}
