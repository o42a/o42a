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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.util.string.ID;


public final class ObjectDataFunc extends Func<ObjectDataFunc> {

	public static final Signature OBJECT_DATA = new Signature();

	private ObjectDataFunc(FuncCaller<ObjectDataFunc> caller) {
		super(caller);
	}

	public final void call(Code code, ObjectIRDataOp data) {
		invoke(null, code, OBJECT_DATA.result(), data);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ObjectDataFunc> {

		private Return<Void> result;
		private Arg<ObjectIRDataOp> data;

		private Signature() {
			super(ID.id("ObjectDataF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ObjectIRDataOp> data() {
			return this.data;
		}

		@Override
		public final ObjectDataFunc op(FuncCaller<ObjectDataFunc> caller) {
			return new ObjectDataFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

}
