/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.AnyOp;


final class DebugFunc extends Func {

	public static final Signature<DebugFunc> DEBUG_SIGNATURE =
		new DebugSignature();

	private DebugFunc(FuncCaller caller) {
		super(caller);
	}

	public void call(Code code, AnyOp message) {
		caller().call(code, message);
	}

	private static final class DebugSignature extends Signature<DebugFunc> {

		DebugSignature() {
			super("void", "DEBUG.DebugF", "wchar_t*");
		}

		@Override
		public DebugFunc op(FuncCaller caller) {
			return new DebugFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<DebugFunc> writer) {
			writer.returnVoid();
			writer.addAny();
		}

	}

}
