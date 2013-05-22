/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.util.string.ID;


public final class ObjectCondFunc extends ObjectFunc<ObjectCondFunc> {

	public static final Signature OBJECT_COND = new Signature();

	private ObjectCondFunc(FuncCaller<ObjectCondFunc> caller) {
		super(caller);
	}

	public final void call(CodeDirs dirs, ObjectOp object) {

		final Block code = dirs.code();
		final BoolOp result = invoke(
				null,
				code,
				OBJECT_COND.result(),
				object != null
				? object.toData(null, code) : code.nullDataPtr());

		result.goUnless(code, dirs.falseDir());
	}

	public static final class Signature
			extends ObjectSignature<ObjectCondFunc> {

		private Return<BoolOp> result;
		private Arg<DataOp> object;

		private Signature() {
			super(ID.id("ObjectCondF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		@Override
		public ObjectCondFunc op(FuncCaller<ObjectCondFunc> caller) {
			return new ObjectCondFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.object = builder.addData("object");
		}

	}

}
