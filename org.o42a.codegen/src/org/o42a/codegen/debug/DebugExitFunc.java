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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;


final class DebugExitFunc extends Func {

	public static final DebugExit DEBUG_EXIT = new DebugExit();

	private DebugExitFunc(FuncCaller<DebugExitFunc> caller) {
		super(caller);
	}

	public void exit(Code code) {
		invoke(code, DEBUG_EXIT.result());
	}

	public static final class DebugExit extends Signature<DebugExitFunc> {

		private Return<Void> result;

		private DebugExit() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		@Override
		public DebugExitFunc op(FuncCaller<DebugExitFunc> caller) {
			return new DebugExitFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("ExitF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
		}

	}

}
