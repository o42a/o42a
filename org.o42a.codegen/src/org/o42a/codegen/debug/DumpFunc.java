/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.codegen.code.op.Int32op;


final class DumpFunc extends Func {

	public static final Signature<DumpFunc> DUMP_SIGNATURE =
		new DumpSignature();

	private DumpFunc(FuncCaller caller) {
		super(caller);
	}

	public void call(Code code, AnyOp data, Int32op depth) {
		caller().call(code, data, depth);
	}

	private static final class DumpSignature
			extends Signature<DumpFunc> {

		DumpSignature() {
			super("void", "DEBUG.DumpF", "void*, uint32_t*");
		}

		@Override
		public DumpFunc op(FuncCaller caller) {
			return new DumpFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<DumpFunc> writer) {
			writer.returnVoid();
			writer.addAny();
			writer.addInt32();
		}

	}

}
