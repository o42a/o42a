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

import static org.o42a.codegen.debug.DebugEnvOp.DEBUG_ENV_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;


final class DebugTraceFunc extends Func {

	public static final DebugTrace DEBUG_TRACE = new DebugTrace();

	private DebugTraceFunc(FuncCaller<DebugTraceFunc> caller) {
		super(caller);
	}

	public void trace(Code code, DebugEnvOp env) {
		invoke(code, DEBUG_TRACE.result(), env);
	}

	public static final class DebugTrace extends Signature<DebugTraceFunc> {

		private Return<Void> result;
		private Arg<DebugEnvOp> env;

		private DebugTrace() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<DebugEnvOp> env() {
			return this.env;
		}

		@Override
		public DebugTraceFunc op(FuncCaller<DebugTraceFunc> caller) {
			return new DebugTraceFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("EnterF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.env = builder.addPtr("env", DEBUG_ENV_TYPE);
		}

	}

}
