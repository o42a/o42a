/*
    Console Module
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
package org.o42a.lib.console;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class DbgExecMainFunc extends Func {

	public static final Signature<DbgExecMainFunc> SIGNATURE =
		new FuncSignature();

	private DbgExecMainFunc(FuncCaller caller) {
		super(caller);
	}

	public Int32op call(Code code, MainFunc main, Int32op argc, AnyOp argv) {
		return caller().callInt32(code, main.toAny(code), argc, argv);
	}

	private static final class FuncSignature
			extends Signature<DbgExecMainFunc> {

		FuncSignature() {
			super(
					"int",
					"console.DbgExecMainF",
					"int(int, char**)*, int, char**");
		}

		@Override
		public DbgExecMainFunc op(FuncCaller caller) {
			return new DbgExecMainFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<DbgExecMainFunc> writer) {
			writer.returnInt32();
			writer.addAny();
			writer.addInt32();
			writer.addAny();
		}

	}

}
