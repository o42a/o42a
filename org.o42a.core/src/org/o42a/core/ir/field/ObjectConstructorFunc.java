/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.AnyOp;


public class ObjectConstructorFunc extends Func {

	ObjectConstructorFunc(FuncCaller caller) {
		super(caller);
	}

	public AnyOp call(Code code, AnyOp object, AnyOp fld) {
		return caller().callAny(code, object, fld);
	}

	static final class ObjectConstructor
			extends Signature<ObjectConstructorFunc> {

		ObjectConstructor() {
			super("any*", "ObjectConstructorF", "any*, Fld*");
		}

		@Override
		public ObjectConstructorFunc op(FuncCaller caller) {
			return new ObjectConstructorFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectConstructorFunc> writer) {
			writer.returnAny();
			writer.addAny();
			writer.addAny();
		}

	}

}
