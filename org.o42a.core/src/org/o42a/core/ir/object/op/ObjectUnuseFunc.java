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

import static org.o42a.core.ir.object.op.ObjectUseOp.OBJECT_USE_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;


public final class ObjectUnuseFunc extends Func<ObjectUnuseFunc> {

	public static final Signature OBJECT_UNUSE = new Signature();

	private ObjectUnuseFunc(FuncCaller<ObjectUnuseFunc> caller) {
		super(caller);
	}

	public final void unuse(Code code, ObjectUseOp.Op use) {
		invoke(null, code, OBJECT_UNUSE.result(), use);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ObjectUnuseFunc> {

		private Return<Void> result;
		private Arg<ObjectUseOp.Op> use;

		private Signature() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ObjectUseOp.Op> use() {
			return this.use;
		}

		@Override
		public final ObjectUnuseFunc op(FuncCaller<ObjectUnuseFunc> caller) {
			return new ObjectUnuseFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ObjectUnuseF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.use = builder.addPtr("use", OBJECT_USE_TYPE);
		}

	}

}
