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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;


/**
 * Link or variable assignment function.
 */
public final class AssignerFunc extends Func {

	AssignerFunc(FuncCaller caller) {
		super(caller);
	}

	public BoolOp assign(Code code, AnyOp object, AnyOp value) {
		return caller().callBool(code, object, value);
	}

	static final class Assigner extends Signature<AssignerFunc> {

		Assigner() {
			super("bool", "AssignerF", "any*, any*");
		}

		@Override
		public AssignerFunc op(FuncCaller caller) {
			return new AssignerFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<AssignerFunc> writer) {
			writer.returnBool();
			writer.addAny();
			writer.addAny();
		}

	}

}
