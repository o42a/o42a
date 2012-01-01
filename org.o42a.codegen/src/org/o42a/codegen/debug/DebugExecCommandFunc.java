/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.DebugEnvOp.DEBUG_ENV_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;


final class DebugExecCommandFunc extends Func<DebugExecCommandFunc> {

	public static final DebugExecCommand DEBUG_EXEC_COMMAND =
			new DebugExecCommand();

	private DebugExecCommandFunc(FuncCaller<DebugExecCommandFunc> caller) {
		super(caller);
	}

	public BoolOp exec(Code code, DebugEnvOp env) {
		return invoke(null, code, DEBUG_EXEC_COMMAND.result(), env);
	}

	public static final class DebugExecCommand
			extends Signature<DebugExecCommandFunc> {

		private Return<BoolOp> result;
		private Arg<DebugEnvOp> env;

		private DebugExecCommand() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<DebugEnvOp> env() {
			return this.env;
		}

		@Override
		public DebugExecCommandFunc op(
				FuncCaller<DebugExecCommandFunc> caller) {
			return new DebugExecCommandFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("EnterF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.env = builder.addPtr("env", DEBUG_ENV_TYPE);
		}

	}

}
