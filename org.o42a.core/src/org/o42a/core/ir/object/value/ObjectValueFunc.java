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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ObjectValueFunc extends ObjectFunc<ObjectValueFunc> {

	public static final Signature OBJECT_VALUE = new Signature();

	private ObjectValueFunc(FuncCaller<ObjectValueFunc> caller) {
		super(caller);
	}

	public final void call(DefDirs dirs, ObjectOp object) {

		final Block code = dirs.code();

		call(
				dirs,
				object != null ? object.objectData(code).ptr() : null,
				object);
	}

	public final void call(
			DefDirs dirs,
			ObjectIRDataOp data,
			ObjectOp object) {
		call(
				dirs,
				data,
				object != null ? object.toData(null, dirs.code()) : null);
	}

	public final void call(DefDirs dirs, ObjectIRDataOp data, DataOp object) {

		final Block code = dirs.code();
		final ValOp value = dirs.value();

		invoke(
				null,
				code,
				OBJECT_VALUE.result(),
				value.ptr(code),
				data != null ? data : code.nullPtr(OBJECT_DATA_TYPE),
				object != null ? object : code.nullDataPtr());

		value.flags(code).condition(null, code).goUnless(code, dirs.falseDir());
		value.holder().set(code);
		dirs.returnValue(value);
	}

	public static final class Signature
			extends ObjectSignature<ObjectValueFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> value;
		private Arg<ObjectIRDataOp> data;
		private Arg<DataOp> object;

		private Signature() {
			super(ID.rawId("o42a_obj_value_ft"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> value() {
			return this.value;
		}

		public final Arg<ObjectIRDataOp> data() {
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
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.value = builder.addPtr("value", VAL_TYPE);
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
			this.object = builder.addData("object");
		}

	}

}
