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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectIRType.Op;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;


public final class CastObjectFunc extends Func {

	public static final CastObject CAST_OBJECT = new CastObject();

	CastObjectFunc(FuncCaller<CastObjectFunc> caller) {
		super(caller);
	}

	public DataOp cast(
			CodeId id,
			Code code,
			ObjectOp object,
			ObjectTypeOp type) {
		return invoke(
				id,
				code,
				CAST_OBJECT.result(),
				object.toData(code), type.ptr());
	}

	public static final class CastObject extends Signature<CastObjectFunc> {

		private Return<DataOp> result;
		private Arg<DataOp> object;
		private Arg<Op> type;

		private CastObject() {
		}

		public final Return<DataOp> result() {
			return this.result;
		}

		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<Op> type() {
			return this.type;
		}

		@Override
		public CastObjectFunc op(FuncCaller<CastObjectFunc> caller) {
			return new CastObjectFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectCastF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnData();
			this.object = builder.addData("object");
			this.type = builder.addPtr("type", OBJECT_TYPE);
		}

	}

}
