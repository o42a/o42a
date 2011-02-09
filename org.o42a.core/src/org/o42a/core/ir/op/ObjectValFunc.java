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


public final class ObjectValFunc extends Func {

	ObjectValFunc(FuncCaller caller) {
		super(caller);
	}

	public void call(Code code, ValOp result, AnyOp object) {
		caller().call(code, result, object);
	}

	static final class ObjectVal extends Signature<ObjectValFunc> {

		private final IRGeneratorBase generator;

		ObjectVal(IRGeneratorBase generator) {
			super("void", "ObjectValF", "val*, any*");
			this.generator = generator;
		}

		@Override
		public ObjectValFunc op(FuncCaller caller) {
			return new ObjectValFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectValFunc> writer) {
			writer.returnVoid();
			writer.addPtr(this.generator.valType());
			writer.addAny();
		}

	}

}
