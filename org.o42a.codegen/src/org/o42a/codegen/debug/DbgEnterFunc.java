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

import static org.o42a.codegen.debug.DbgStackFrameType.DBG_STACK_FRAME_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;


final class DbgEnterFunc extends Func {

	public static final Signature<DbgEnterFunc> DBG_ENTER =
		new DbgEnter();

	private DbgEnterFunc(FuncCaller caller) {
		super(caller);
	}

	public void enter(Code code, DbgStackFrameType.Op stackFrame) {
		caller().call(code, stackFrame);
	}

	private static final class DbgEnter extends Signature<DbgEnterFunc> {

		DbgEnter() {
			super("void", "DEBUG.EnterF", "DEBUG.StackFrame*");
		}

		@Override
		public DbgEnterFunc op(FuncCaller caller) {
			return new DbgEnterFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<DbgEnterFunc> writer) {
			writer.returnVoid();
			writer.addPtr(DBG_STACK_FRAME_TYPE);
		}

	}

}
