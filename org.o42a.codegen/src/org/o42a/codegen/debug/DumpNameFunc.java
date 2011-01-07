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


final class DumpNameFunc extends Func {

	public static final Signature<DumpNameFunc> DUMP_NAME_SIGNATURE =
		new DumpNameSignature();

	private DumpNameFunc(FuncCaller caller) {
		super(caller);
	}

	public void call(Code code, AnyOp message, AnyOp data) {
		caller().call(code, message, data);
	}

	private static final class DumpNameSignature
			extends Signature<DumpNameFunc> {

		DumpNameSignature() {
			super("void", "DEBUG.DumpNameF", "wchar_t*, void*");
		}

		@Override
		public DumpNameFunc op(FuncCaller caller) {
			return new DumpNameFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<DumpNameFunc> writer) {
			writer.returnVoid();
			writer.addAny();
			writer.addAny();
		}

	}

}
