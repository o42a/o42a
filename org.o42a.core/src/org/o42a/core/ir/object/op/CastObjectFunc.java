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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.util.string.ID;


public final class CastObjectFunc extends Func<CastObjectFunc> {

	public static final Signature CAST_OBJECT = new Signature();

	CastObjectFunc(FuncCaller<CastObjectFunc> caller) {
		super(caller);
	}

	public DataOp cast(ID id, Code code, ObjectOp object, ObjectTypeOp type) {
		return invoke(
				id,
				code,
				CAST_OBJECT.result(),
				object.toData(null, code), type.ptr());
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<CastObjectFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;
		private Arg<ObjectIRTypeOp> type;

		private Signature() {
			super(ID.id("ObjectCastF"));
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<ObjectIRTypeOp> type() {
			return this.type;
		}

		@Override
		public CastObjectFunc op(FuncCaller<CastObjectFunc> caller) {
			return new CastObjectFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
			this.type = builder.addPtr("type", OBJECT_TYPE);
		}

	}

}
