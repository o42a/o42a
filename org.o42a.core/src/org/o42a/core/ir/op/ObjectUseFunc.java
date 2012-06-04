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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.op.ObjectUseOp.OBJECT_USE_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.ObjectIRData;


public final class ObjectUseFunc extends Func<ObjectUseFunc> {

	public static final Signature OBJECT_USE = new Signature();

	private ObjectUseFunc(FuncCaller<ObjectUseFunc> caller) {
		super(caller);
	}

	public final void use(
			Code code,
			ObjectUseOp.Op use,
			ObjectIRData.Op data) {
		invoke(null, code, OBJECT_USE.result(), use, data);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ObjectUseFunc> {

		private Return<Void> result;
		private Arg<ObjectUseOp.Op> use;
		private Arg<ObjectIRData.Op> data;

		private Signature() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ObjectUseOp.Op> use() {
			return this.use;
		}

		public final Arg<ObjectIRData.Op> data() {
			return this.data;
		}

		@Override
		public final ObjectUseFunc op(FuncCaller<ObjectUseFunc> caller) {
			return new ObjectUseFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectUseF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.use = builder.addPtr("use", OBJECT_USE_TYPE);
			this.data = builder.addPtr("data", OBJECT_DATA_TYPE);
		}

	}

}
