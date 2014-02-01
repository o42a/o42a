/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.codegen.debug.Debug.DEBUG_ID;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;


final class DebugEnterFunc extends Func<DebugEnterFunc> {

	static final Signature DEBUG_ENTER = new Signature();

	private DebugEnterFunc(FuncCaller<DebugEnterFunc> caller) {
		super(caller);
	}

	public BoolOp enter(Code code, DebugStackFrameOp stackFrame) {
		return invoke(null, code, DEBUG_ENTER.result(), stackFrame);
	}

	static final class Signature
			extends org.o42a.codegen.code.Signature<DebugEnterFunc> {

		private Return<BoolOp> result;
		private Arg<DebugStackFrameOp> stackFrame;

		private Signature() {
			super(DEBUG_ID.sub("EnterF"));
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<DebugStackFrameOp> stackFrame() {
			return this.stackFrame;
		}

		@Override
		public DebugEnterFunc op(FuncCaller<DebugEnterFunc> caller) {
			return new DebugEnterFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.stackFrame = builder.addPtr("env", DEBUG_STACK_FRAME_TYPE);
		}

	}

}
