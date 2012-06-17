/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ir.field.variable;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.util.string.ID;


public class VariableAssignerFunc extends ObjectFunc<VariableAssignerFunc> {

	public static final Signature VARIABLE_ASSIGNER =
			new Signature();

	private VariableAssignerFunc(FuncCaller<VariableAssignerFunc> caller) {
		super(caller);
	}

	public void assign(CodeDirs dirs, ObjectOp object, ObjectOp value) {

		final Block code = dirs.code();

		assign(code, object, value).goUnless(code, dirs.falseDir());
	}

	public BoolOp assign(Code code, ObjectOp object, ObjectOp value) {
		return invoke(
				null,
				code,
				VARIABLE_ASSIGNER.result(),
				object != null ? object.toData(null, code) : code.nullDataPtr(),
				value.toData(null, code));
	}

	public static final class Signature
			extends ObjectSignature<VariableAssignerFunc> {

		private Return<BoolOp> result;
		private Arg<DataOp> object;
		private Arg<DataOp> value;

		private Signature() {
			super(ID.id("VariableAssignerF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		@Override
		public final Arg<DataOp> object() {
			return this.object;
		}

		public final Arg<DataOp> value() {
			return this.value;
		}

		@Override
		public VariableAssignerFunc op(
				FuncCaller<VariableAssignerFunc> caller) {
			return new VariableAssignerFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.object = builder.addData("object");
			this.value = builder.addData("value");
		}

	}

}
