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
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ObjectValueStartFunc extends Func<ObjectValueStartFunc> {

	public static final Signature OBJECT_VALUE_START = new Signature();

	private ObjectValueStartFunc(FuncCaller<ObjectValueStartFunc> caller) {
		super(caller);
	}

	public final BoolOp call(Code code, ValOp value, ObjectIRData.Op data) {
		return invoke(
				null,
				code,
				OBJECT_VALUE_START.result(),
				value.ptr(),
				data);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ObjectValueStartFunc> {

		private Return<BoolOp> result;
		private Arg<ValType.Op> value;
		private Arg<ObjectIRData.Op> data;

		private Signature() {
			super(ID.id("ObjectDataCondF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<ValType.Op> value() {
			return this.value;
		}

		public final Arg<ObjectIRData.Op> data() {
			return this.data;
		}

		@Override
		public final ObjectValueStartFunc op(
				FuncCaller<ObjectValueStartFunc> caller) {
			return new ObjectValueStartFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.value = builder.addPtr("value", VAL_TYPE);
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

}
